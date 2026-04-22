package com.isysnap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(PENDING|CONFIRMED|PREPARING|READY|DELIVERED|CANCELLED)$",
             message = "Status must be one of: PENDING, CONFIRMED, PREPARING, READY, DELIVERED, CANCELLED")
    private String status;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}