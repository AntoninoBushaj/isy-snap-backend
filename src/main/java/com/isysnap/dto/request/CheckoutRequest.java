package com.isysnap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CheckoutRequest {
    // sessionId, guestToken extracted from JWT in Authorization header

    @NotBlank(message = "Payment provider is required")
    @Pattern(regexp = "^(STRIPE|PAYPAL|MOCK)$", message = "Provider must be STRIPE, PAYPAL, or MOCK")
    private String provider;
}