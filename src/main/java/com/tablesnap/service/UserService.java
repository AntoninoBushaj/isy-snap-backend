package com.tablesnap.service;

import com.tablesnap.dto.request.AuthRequest;
import com.tablesnap.dto.request.RegisterRequest;
import com.tablesnap.dto.response.AuthResponse;
import com.tablesnap.entity.User;
import com.tablesnap.exception.AuthenticationException;
import com.tablesnap.repository.UserRepository;
import com.tablesnap.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

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
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId());

        // Genera token JWT con restaurantId vuoto (verrà impostato quando l'utente accede a un ristorante)
        String token = jwtTokenProvider.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "" // Default empty restaurant ID for now
        );

        // Genera refresh token
        String refreshToken = refreshTokenService.createRefreshToken(
                savedUser.getId(),
                "" // Default empty restaurant ID for now
        );

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .message("User registered successfully")
                .build();
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Trova user per email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        // Verifica password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        log.info("Login successful for user: {}", user.getId());

        // Genera token JWT con restaurantId vuoto (verrà impostato quando l'utente accede a un ristorante)
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                "" // Default empty restaurant ID for now
        );

        // Genera refresh token
        String refreshToken = refreshTokenService.createRefreshToken(
                user.getId(),
                "" // Default empty restaurant ID for now
        );

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    @Transactional(readOnly = true)
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));
    }
}