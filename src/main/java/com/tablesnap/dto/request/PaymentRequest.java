package com.tablesnap.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Payment method is required")
    private String method; // "card", "upi", "wallet"

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private CardDetails cardDetails;
    private String upiId;
    private String walletId;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CardDetails {
        private String holderName;
        private String cardNumber;
        private String expiryDate;
        private String cvv;
    }
}