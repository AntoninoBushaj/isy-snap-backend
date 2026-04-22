package com.isysnap.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateRestaurantRequest {

    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Pattern(regexp = "^[+\\d\\s()\\-]*$", message = "Phone number contains invalid characters")
    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;

    private String logo;
}