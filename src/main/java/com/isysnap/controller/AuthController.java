package com.isysnap.controller;

import com.isysnap.dto.request.AuthRequest;
import com.isysnap.dto.request.RegisterRequest;
import com.isysnap.dto.response.AuthResponse;
import com.isysnap.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "🔐 Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final UserService userService;

    @Operation(
        operationId = "register",
        summary = "Register a new user (ADMIN only)",
        description = "Register a new user account (ADMIN, STAFF, or CUSTOMER). Only ADMIN users can create new accounts.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("hasRole('ADMIN')")
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
        description = "Logout user (stateless - client should delete JWT from localStorage)"
    )
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // JWT è stateless, non serve revoca lato server
        // Il frontend deve eliminare il token da localStorage
        return ResponseEntity.ok("Logout successful");
    }
}