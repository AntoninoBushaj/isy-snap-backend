package com.isysnap.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^(ADMIN|STAFF|CUSTOMER)$", message = "Role must be ADMIN, STAFF, or CUSTOMER")
    private String role;

    private String restaurantId;
}
