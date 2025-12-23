package com.isysnap.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CheckoutResponse {
    private String paymentId;
    private BigDecimal amount;
    private String providerUrl;
    private String orderId;
}