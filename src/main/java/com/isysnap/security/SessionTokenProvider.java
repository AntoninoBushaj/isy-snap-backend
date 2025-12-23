package com.isysnap.security;

import com.isysnap.dto.SessionTokenClaims;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Provider for generating and validating JWT tokens for dining sessions.
 * These tokens are issued after QR code validation and authorize customer
 * access to table-specific operations.
 */
@Component
@Slf4j
public class SessionTokenProvider {

    @Value("${guest.jwt.secret:change-this-guest-jwt-secret-min-32-chars-for-hs256}")
    private String jwtSecret;

    @Value("${guest.jwt.expiration:14400000}") // 4 hours default
    private Long jwtExpiration;

    /**
     * Generate a session JWT token after successful QR code validation
     *
     * @param sessionId     Dining session ID (table session)
     * @param guestId       Guest ID (unique person at the table)
     * @param tableId       Table ID
     * @param restaurantId  Restaurant ID
     * @param tableCode     Table code (e.g., "T1")
     * @param guestNumber   Guest number from dining_session_guests (1, 2, 3...)
     * @param qrSlug        QR code slug for validation
     * @return JWT token string
     */
    public String generateSessionToken(String sessionId, String guestId, String tableId,
                                        String restaurantId, String tableCode, Integer guestNumber,
                                        String qrSlug) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject(guestId)  // Subject is now guestId (identifies the person)
                .claim("sessionId", sessionId)
                .claim("guestId", guestId)
                .claim("tableId", tableId)
                .claim("restaurantId", restaurantId)
                .claim("tableCode", tableCode)
                .claim("guestNumber", guestNumber)
                .claim("qrSlug", qrSlug)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.info("Generated session JWT for guest: {}, session: {}, table: {}, guest#: {}, expires at: {}",
                guestId, sessionId, tableCode, guestNumber, expiryDate);

        return token;
    }

    /**
     * Validate a session JWT token
     *
     * @param token JWT token string
     * @return true if valid and not expired, false otherwise
     */
    public boolean validateSessionToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("Session JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract all claims from a valid session JWT token
     *
     * @param token JWT token string
     * @return SessionTokenClaims object with all extracted data
     */
    public SessionTokenClaims getClaimsFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return SessionTokenClaims.builder()
                    .sessionId(claims.get("sessionId", String.class))
                    .guestId(claims.get("guestId", String.class))
                    .tableId(claims.get("tableId", String.class))
                    .restaurantId(claims.get("restaurantId", String.class))
                    .tableCode(claims.get("tableCode", String.class))
                    .guestNumber(claims.get("guestNumber", Integer.class))
                    .qrSlug(claims.get("qrSlug", String.class))
                    .iat(claims.getIssuedAt().getTime())
                    .exp(claims.getExpiration().getTime())
                    .build();
        } catch (Exception e) {
            log.error("Failed to extract claims from JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract session ID from token without full validation
     *
     * @param token JWT token string
     * @return session ID or null if extraction fails
     */
    public String getSessionIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("sessionId", String.class);
        } catch (Exception e) {
            log.error("Failed to extract session ID from JWT: {}", e.getMessage());
            return null;
        }
    }
}