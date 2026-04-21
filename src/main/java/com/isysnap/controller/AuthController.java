package com.isysnap.controller;

import com.isysnap.dto.request.AuthRequest;
import com.isysnap.dto.request.RegisterRequest;
import com.isysnap.dto.response.AuthResponse;
import com.isysnap.repository.TokenRepository;
import com.isysnap.security.JwtTokenProvider;
import com.isysnap.security.TokenRevocationCache;
import com.isysnap.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "🔐 Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final UserService userService;
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRevocationCache tokenRevocationCache;

    @Operation(
        operationId = "register",
        summary = "Register a new user (ADMIN only)",
        description = "Register a new user account (ADMIN, STAFF, or CUSTOMER). Only ADMIN users can create new accounts.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasAuthority('user:create')")
    @PostMapping("/registerUser")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        operationId = "login",
        summary = "User login",
        description = "Authenticate user and receive JWT access token"
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        operationId = "logout",
        summary = "User logout",
        description = "Logout user: revokes the current JWT server-side and invalidates all user tokens in the database",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @Transactional
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, Authentication authentication) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ") && authentication != null) {
            String jwt = authHeader.substring(7);
            String userId = (String) authentication.getPrincipal();

            // Add to in-memory revocation cache for immediate effect
            tokenRevocationCache.revoke(jwt, jwtTokenProvider.getExpirationFromToken(jwt));

            // Invalidate all tokens in the database for this user
            tokenRepository.invalidateAllUserTokens(userId);
        }
        return ResponseEntity.ok("Logout successful");
    }
}