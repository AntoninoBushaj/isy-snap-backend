package com.tablesnap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String id;
    private String orderId;
    private String transactionId;
    private String method;
    private BigDecimal amount;
    private String status;
    private Instant processedAt;

}