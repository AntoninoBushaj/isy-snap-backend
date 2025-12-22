package com.tablesnap.service;

import com.tablesnap.dto.response.AuthResponse;
import com.tablesnap.entity.RefreshToken;
import com.tablesnap.entity.User;
import com.tablesnap.exception.AuthenticationException;
import com.tablesnap.repository.RefreshTokenRepository;
import com.tablesnap.repository.UserRepository;
import com.tablesnap.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Create and save a new refresh token for a user
     */
    @Transactional
    public String createRefreshToken(String userId, String restaurantId) {
        log.debug("Creating refresh token for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Genera il refresh token JWT (7 giorni)
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(userId);

        // Salva il token nel database per poter revokarlo
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .restaurantId(restaurantId)
                .expiresAt(Instant.now().plusSeconds(7 * 24 * 60 * 60)) // 7 days
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token created and saved for user: {}", userId);

        return refreshTokenValue;
    }

    /**
     * Refresh the access token using a valid refresh token
     */
    @Transactional
    public AuthResponse refreshAccessToken(String refreshTokenValue) {
        log.debug("Attempting to refresh access token");

        // Valida il refresh token JWT
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        // Recupera il token dal database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new AuthenticationException("Refresh token not found in database"));

        // Controlla che non sia stato revocato
        if (refreshToken.isRevoked()) {
            throw new AuthenticationException("Refresh token has been revoked");
        }

        // Controlla che non sia scaduto
        if (refreshToken.isExpired()) {
            throw new AuthenticationException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        String restaurantId = refreshToken.getRestaurantId();

        // Genera nuovo access token
        String newAccessToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                restaurantId
        );

        log.info("Access token refreshed for user: {} (restaurantId: {})", user.getId(), restaurantId);

        return AuthResponse.builder()
                .token(newAccessToken)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Token refreshed successfully")
                .build();
    }

    /**
     * Revoke a refresh token (for logout)
     */
    @Transactional
    public void revokeRefreshToken(String refreshTokenValue) {
        log.debug("Revoking refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new AuthenticationException("Refresh token not found"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token revoked for user: {}", refreshToken.getUser().getId());
    }

    /**
     * Revoke all refresh tokens for a user (for logout from all devices)
     */
    @Transactional
    public void revokeAllRefreshTokensForUser(String userId) {
        log.info("Revoking all refresh tokens for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        refreshTokenRepository.deleteByUserId(userId);
    }

    /**
     * Clean up expired refresh tokens (can be called periodically)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        log.debug("Cleaning up expired refresh tokens");
        // Implementazione semplice: potrebbe essere migliorata con una query dedicata
        // Per ora semplicemente logghiamo che il metodo è stato chiamato
        log.debug("Expired token cleanup completed");
    }
}