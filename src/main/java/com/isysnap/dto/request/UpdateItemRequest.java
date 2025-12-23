package com.isysnap.dto.request;

import lombok.Data;

@Data
public class UpdateItemRequest {
    // sessionId, guestToken extracted from JWT in Authorization header
    private Integer quantity;
}