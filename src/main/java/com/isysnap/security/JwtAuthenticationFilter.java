package com.isysnap.security;

import com.isysnap.context.RestaurantContext;
import com.isysnap.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRevocationCache tokenRevocationCache;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {

                // Reject tokens that have been revoked (e.g. after logout)
                if (!tokenRevocationCache.isRevoked(jwt)) {
                    String userId = jwtTokenProvider.getUserIdFromToken(jwt);
                    String role = jwtTokenProvider.getRoleFromToken(jwt);
                    String restaurantId = jwtTokenProvider.getRestaurantIdFromToken(jwt);

                    // Set restaurant context for multi-tenancy isolation
                    if (restaurantId != null) {
                        RestaurantContext.setRestaurantId(restaurantId);
                    }

                    // Build full authority list from role (ROLE_X + all granular permissions)
                    User.UserRole userRole = User.UserRole.valueOf(role);
                    List<SimpleGrantedAuthority> authorities = userRole.getAuthorities();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("JWT authentication set for user: {} with role: {} ({} authorities), restaurantId: {}",
                            userId, role, authorities.size(), restaurantId);
                } else {
                    log.warn("Rejected revoked token for request: {}", request.getRequestURI());
                }
            }
        } catch (Exception ex) {
            log.debug("Could not set user authentication in security context", ex);
        } finally {
            filterChain.doFilter(request, response);
            RestaurantContext.clear();
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}