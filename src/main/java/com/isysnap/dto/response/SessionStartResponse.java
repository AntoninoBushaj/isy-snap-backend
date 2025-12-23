package com.isysnap.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionStartResponse {
    private String sessionId;     // Session ID used for all subsequent API calls
    private String guestId;
    private Integer guestNumber;
}