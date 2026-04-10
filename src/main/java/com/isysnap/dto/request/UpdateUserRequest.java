package com.isysnap.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String role;
    private String restaurantId;
}
