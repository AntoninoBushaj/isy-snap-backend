package com.isysnap.dto.request;

import lombok.Data;

@Data
public class CheckoutRequest {
    // sessionId, guestToken extracted from JWT in Authorization header
    private String provider;       // "STRIPE", "PAYPAL", or "MOCK"
}