package com.isysnap.security;

import com.isysnap.dto.SessionTokenClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authentication filter for guest session JWT tokens.
 * Validates JWT from Authorization header and populates SessionAuthContext.
 * This runs AFTER JwtAuthenticationFilter (for staff/admin authentication).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionGuestAuthenticationFilter extends OncePerRequestFilter {

    private final SessionTokenProvider sessionTokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract JWT from Authorization header
            String jwt = extractJwtFromRequest(request);

            if (jwt != null) {
                // Validate and extract claims
                if (sessionTokenProvider.validateSessionToken(jwt)) {
                    SessionTokenClaims claims = sessionTokenProvider.getClaimsFromToken(jwt);

                    if (claims != null) {
                        // Store claims in ThreadLocal context for this request
                        SessionAuthContext.setSession(claims);
                        log.debug("Session JWT validated for guest: {}, session: {}",
                                claims.getGuestId(), claims.getSessionId());
                    }
                } else {
                    log.warn("Invalid session JWT token");
                }
            }

            // Continue filter chain
            filterChain.doFilter(request, response);
        } finally {
            // CRITICAL: Clear context after request to prevent memory leaks
            SessionAuthContext.clear();
        }
    }

    /**
     * Extract JWT token from Authorization header
     * Format: "Bearer <token>"
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }

        return null;
    }

    /**
     * Determine if this filter should run for the given request.
     * We want to run for all guest endpoints (menu, orders, payments).
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // Skip filter for public endpoints that don't need session JWT
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/qr/authorizeQr") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/api/dev/");
    }
}
