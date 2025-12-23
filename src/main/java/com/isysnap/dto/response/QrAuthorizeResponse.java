package com.isysnap.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QrAuthorizeResponse {
    private Boolean valid;
    private String sessionToken;    // JWT token for session authentication
    private String sessionId;       // Session ID for frontend storage
    private TableInfoResponse tableInfo;
    private RestaurantInfoResponse restaurantInfo;
}