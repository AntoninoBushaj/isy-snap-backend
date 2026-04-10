package com.isysnap.service;

import com.isysnap.dto.UserDTO;
import com.isysnap.dto.request.AuthRequest;
import com.isysnap.dto.request.RegisterRequest;
import com.isysnap.dto.request.UpdateUserRequest;
import com.isysnap.dto.response.AuthResponse;
import com.isysnap.entity.RestaurantUser;
import com.isysnap.entity.Token;
import com.isysnap.entity.User;
import com.isysnap.exception.AuthenticationException;
import com.isysnap.repository.RestaurantRepository;
import com.isysnap.repository.RestaurantUserRepository;
import com.isysnap.repository.TokenRepository;
import com.isysnap.repository.UserRepository;
import com.isysnap.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RestaurantUserRepository restaurantUserRepository;
    private final RestaurantRepository restaurantRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        // Verifica che l'email non esista già
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already registered");
        }

        // Valida il role
        User.UserRole role;
        try {
            role = User.UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Invalid role: " + request.getRole());
        }

        // Crea nuovo user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} with role: {}", savedUser.getId(), savedUser.getRole());

        // Genera token JWT con restaurantId vuoto (verrà impostato al login se STAFF)
        String jwtToken = jwtTokenProvider.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "" // Default empty restaurant ID for now
        );

        // Salva token nel database
        saveUserToken(savedUser, jwtToken);

        return AuthResponse.builder()
                .token(jwtToken)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .message("User registered successfully")
                .build();
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            // Use Spring Security's AuthenticationManager to authenticate
            authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            log.warn("Authentication failed for user: {}", request.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }

        // Trova user per email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        // Aggiorna lastLogin
        user.setLastLogin(Instant.now());
        userRepository.save(user);

        log.info("Login successful for user: {}", user.getId());

        // Determina restaurantId basato sul ruolo dell'utente
        String restaurantId = null;

        if (user.getRole() == User.UserRole.STAFF) {
            // Se è STAFF, cerca il ristorante associato
            List<RestaurantUser> restaurantUsers = restaurantUserRepository.findActiveByUserId(user.getId());

            if (restaurantUsers.isEmpty()) {
                // Permetti login anche senza ristoranti assegnati
                restaurantId = null;
                log.warn("STAFF user {} not assigned to any restaurant - login allowed with no restaurant access", user.getId());
            } else {
                // Prendi il primo ristorante attivo (in futuro si può gestire multi-restaurant)
                restaurantId = restaurantUsers.get(0).getRestaurant().getId();
                log.info("STAFF user {} assigned to restaurant: {}", user.getId(), restaurantId);
            }
        } else if (user.getRole() == User.UserRole.ADMIN) {
            // ADMIN non ha restaurantId (vede tutto)
            restaurantId = null;
            log.info("ADMIN user {} - no restaurant filtering", user.getId());
        } else if (user.getRole() == User.UserRole.CUSTOMER) {
            // CUSTOMER non ha restaurantId (vede solo i propri ordini)
            restaurantId = null;
            log.info("CUSTOMER user {} - will see only own orders", user.getId());
        }

        // Check for existing valid tokens
        List<Token> validTokens = tokenRepository.findAllValidTokenByUser(user.getEmail());
        Optional<Token> matchingToken = validTokens.stream()
                .filter(t -> jwtTokenProvider.validateToken(t.getToken()))
                .findFirst();

        if (matchingToken.isPresent()) {
            log.info("Reusing existing valid token for user: {}", user.getId());
            return AuthResponse.builder()
                    .token(matchingToken.get().getToken())
                    .userId(user.getId())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .restaurantId(restaurantId)
                    .message("Login successful")
                    .build();
        }

        // Se non ci sono token validi, invalida tutti i vecchi e genera uno nuovo
        tokenRepository.invalidateAllUserTokens(user.getId());

        // Genera nuovo token JWT con restaurantId corretto
        String jwtToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                restaurantId != null ? restaurantId : ""
        );

        // Salva nuovo token nel database
        saveUserToken(user, jwtToken);

        return AuthResponse.builder()
                .token(jwtToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .restaurantId(restaurantId)
                .message("Login successful")
                .build();
    }

    @Transactional(readOnly = true)
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream()
                .map(user -> {
                    UserDTO dto = UserDTO.fromEntity(user);
                    // Attach restaurant info for STAFF users
                    if (user.getRole() == User.UserRole.STAFF) {
                        List<RestaurantUser> assignments = restaurantUserRepository.findActiveByUserId(user.getId());
                        if (!assignments.isEmpty()) {
                            dto.setRestaurantId(assignments.get(0).getRestaurant().getId());
                            dto.setRestaurantName(assignments.get(0).getRestaurant().getName());
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserDTOById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));
        UserDTO dto = UserDTO.fromEntity(user);
        if (user.getRole() == User.UserRole.STAFF) {
            List<RestaurantUser> assignments = restaurantUserRepository.findActiveByUserId(userId);
            if (!assignments.isEmpty()) {
                dto.setRestaurantId(assignments.get(0).getRestaurant().getId());
                dto.setRestaurantName(assignments.get(0).getRestaurant().getName());
            }
        }
        return dto;
    }

    @Transactional
    public UserDTO updateUser(String userId, UpdateUserRequest request) {
        log.info("Updating user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new AuthenticationException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getRole() != null && !request.getRole().isBlank()) {
            User.UserRole newRole;
            try {
                newRole = User.UserRole.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AuthenticationException("Invalid role: " + request.getRole());
            }
            // Prevent removing last ADMIN
            if (user.getRole() == User.UserRole.ADMIN && newRole != User.UserRole.ADMIN) {
                long adminCount = userRepository.countByRole(User.UserRole.ADMIN);
                if (adminCount <= 1) {
                    throw new AuthenticationException("Cannot change role of the last ADMIN");
                }
            }
            user.setRole(newRole);
        }

        User saved = userRepository.save(user);
        return UserDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Prevent deleting last ADMIN
        if (user.getRole() == User.UserRole.ADMIN) {
            long adminCount = userRepository.countByRole(User.UserRole.ADMIN);
            if (adminCount <= 1) {
                throw new AuthenticationException("Cannot delete the last ADMIN user");
            }
        }

        // Invalidate all tokens
        tokenRepository.invalidateAllUserTokens(userId);

        userRepository.deleteById(userId);
        log.info("User {} deleted successfully", userId);
    }

    private void saveUserToken(User user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
}