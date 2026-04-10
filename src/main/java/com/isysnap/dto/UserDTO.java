package com.isysnap.dto;

import com.isysnap.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserDTO {

    private String id;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
    private Instant createdAt;
    private Instant lastLogin;

    // Populated separately when available
    private String restaurantId;
    private String restaurantName;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}