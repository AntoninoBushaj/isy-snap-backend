package com.isysnap.dto.request;

import lombok.Data;

@Data
public class SessionStartRequest {
    // DEPRECATED: This API is no longer used
    // Session and guest creation now happens via /api/qr/authorizeQr
    // which returns a JWT containing sessionId and guestId
}