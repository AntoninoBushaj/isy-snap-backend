package com.isysnap.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Bucket4j (token bucket algorithm).
 * Limits are applied per client IP address per endpoint group.
 *
 * Limits:
 *   /api/auth/login        → 5 requests/minute
 *   /api/auth/registerUser → 3 requests/minute
 *   /api/payments/**       → 10 requests/minute
 *   All other endpoints    → 60 requests/minute
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> registerBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> paymentBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> defaultBuckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String ip = getClientIp(request);
        String path = request.getRequestURI();

        Bucket bucket = resolveBucket(ip, path);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP: {} on path: {}", ip, path);
            response.setContentType("application/json");
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                    "error", "Too many requests. Please try again later.",
                    "errorCode", "RATE_LIMIT_EXCEEDED",
                    "timestamp", Instant.now().toString(),
                    "path", path
            )));
        }
    }

    private Bucket resolveBucket(String ip, String path) {
        if (path.equals("/api/auth/login")) {
            return loginBuckets.computeIfAbsent(ip, k -> buildBucket(5, Duration.ofMinutes(1)));
        } else if (path.equals("/api/auth/registerUser")) {
            return registerBuckets.computeIfAbsent(ip, k -> buildBucket(3, Duration.ofMinutes(1)));
        } else if (path.startsWith("/api/payments/")) {
            return paymentBuckets.computeIfAbsent(ip, k -> buildBucket(10, Duration.ofMinutes(1)));
        } else {
            return defaultBuckets.computeIfAbsent(ip, k -> buildBucket(60, Duration.ofMinutes(1)));
        }
    }

    private Bucket buildBucket(int capacity, Duration refillDuration) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, refillDuration));
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip for static/swagger resources and payment webhooks (machine-to-machine)
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/api/payments/processWebhook");
    }
}
