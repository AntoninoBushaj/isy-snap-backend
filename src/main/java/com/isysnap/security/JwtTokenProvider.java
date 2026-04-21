package com.isysnap.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

@Component
@Slf4j
public class JwtTokenProvider {

    private static final int MIN_SECRET_LENGTH = 32;
    private static final Set<String> KNOWN_INSECURE_DEFAULTS = Set.of(
            "tablesnap-super-secret-key-change-in-production-min-32-chars",
            "isysnap-super-secret-key-change-in-production-min-32-chars-required-for-hs256"
    );

    private final String rawSecret;
    private final SecretKey jwtSecret;
    private final long jwtExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:86400000}") long expirationMs) {
        this.rawSecret = secret;
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationMs = expirationMs;
    }

    @PostConstruct
    public void validateSecrets() {
        if (rawSecret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT secret is too short. Minimum length is " + MIN_SECRET_LENGTH + " characters.");
        }
        if (KNOWN_INSECURE_DEFAULTS.contains(rawSecret)) {
            log.warn("WARNING: JWT secret is set to a known insecure default value. " +
                     "Set the JWT_SECRET environment variable to a strong random secret before deploying to production.");
        }
    }

    public String generateToken(String userId, String email, String role) {
        return generateToken(userId, email, role, null);
    }

    public String generateToken(String userId, String email, String role, String restaurantId) {
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("role", role)
                .claim("restaurantId", restaurantId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(jwtSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        long refreshExpirationMs = 7 * 24 * 60 * 60 * 1000; // 7 days
        return Jwts.builder()
                .subject(userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(jwtSecret, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(jwtSecret)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getSubject();
    }

    public String getEmailFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("email", String.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    public String getRestaurantIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("restaurantId", String.class);
    }

    public Instant getExpirationFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getExpiration().toInstant();
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}