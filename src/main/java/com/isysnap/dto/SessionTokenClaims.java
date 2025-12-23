package com.isysnap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionTokenClaims {
    private String sessionId;
    private String guestId;
    private String tableId;
    private String restaurantId;
    private String tableCode;
    private Integer guestNumber;
    private String qrSlug;
    private Long iat;  // issued at timestamp
    private Long exp;  // expiration timestamp
}