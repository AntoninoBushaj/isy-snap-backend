package com.isysnap.dto;

import com.isysnap.entity.PaymentItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentItemDTO {

    private String id;
    private String paymentId;    // Solo ID, NO oggetto Payment nested
    private String orderItemId;  // Solo ID, NO oggetto OrderItem nested
    private BigDecimal amount;
    private Instant createdAt;

    public static PaymentItemDTO fromEntity(PaymentItem entity) {
        if (entity == null) {
            return null;
        }

        return PaymentItemDTO.builder()
                .id(entity.getId())
                .paymentId(entity.getPayment() != null ? entity.getPayment().getId() : null)
                .orderItemId(entity.getOrderItem() != null ? entity.getOrderItem().getId() : null)
                .amount(entity.getAmount())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}