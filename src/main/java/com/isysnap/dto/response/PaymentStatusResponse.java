package com.isysnap.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class PaymentStatusResponse {
    private String paymentId;
    private String status;
    private OrderStatusResponse order;

    @Data
    @Builder
    public static class OrderStatusResponse {
        private String status;
        private Instant confirmedAt;
    }
}