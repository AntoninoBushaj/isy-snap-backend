package com.isysnap.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class SessionInfoResponse {
    private String id;
    private String status;
    private String tableCode;
    private String restaurantName;
    private Instant openedAt;
}