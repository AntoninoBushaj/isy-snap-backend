package com.isysnap.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory cache of revoked tokens.
 * Avoids a DB query on every request after the token has been invalidated.
 * Entries are stored with their expiry time and cleaned up lazily on access.
 */
@Component
public class TokenRevocationCache {

    private final ConcurrentHashMap<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    /**
     * Mark a token as revoked until the given expiry instant.
     */
    public void revoke(String token, Instant expiresAt) {
        revokedTokens.put(token, expiresAt);
    }

    /**
     * Returns true if the token is currently in the revocation cache.
     * Expired entries are removed lazily.
     */
    public boolean isRevoked(String token) {
        Instant expiresAt = revokedTokens.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (Instant.now().isAfter(expiresAt)) {
            // JWT has expired anyway — clean up and report as not revoked
            // (the JWT signature validation will reject it)
            revokedTokens.remove(token);
            return false;
        }
        return true;
    }
}
