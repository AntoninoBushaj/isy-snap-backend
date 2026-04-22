package com.isysnap.controller;

import com.isysnap.dto.UserDTO;
import com.isysnap.dto.request.UpdateUserRequest;
import com.isysnap.dto.response.SuccessResponse;
import com.isysnap.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "👤 Users", description = "User management endpoints (ADMIN only)")
public class UserController {

    private final UserService userService;

    @GetMapping("/getAll")
    @PreAuthorize("hasAuthority('user:read')")
    @Operation(
        summary = "Get all users",
        description = "ADMIN: Get all registered users",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:read')")
    @Operation(
        summary = "Get user by ID",
        description = "ADMIN: Get a specific user by ID",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserDTO> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserDTOById(userId));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:update')")
    @Operation(
        summary = "Update user",
        description = "ADMIN: Update user role or email",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:delete')")
    @Operation(
        summary = "Delete user",
        description = "ADMIN: Delete a user. Cannot delete the last ADMIN.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<SuccessResponse> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(SuccessResponse.of(true));
    }
}
