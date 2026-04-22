package com.isysnap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class AddItemRequest {
    // sessionId, guestToken extracted from JWT in Authorization header

    @NotBlank(message = "Menu item ID is required")
    private String menuItemId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be at least 1")
    private Integer quantity;

    private List<String> optionIds;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}