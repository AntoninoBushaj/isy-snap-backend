package com.isysnap.dto;

import com.isysnap.entity.Payment;
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
public class PaymentDTO {

    private String id;
    private String diningSessionId;  // Solo ID, NO oggetto DiningSession nested
    private String provider;
    private String providerReference;
    private BigDecimal amount;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static PaymentDTO fromEntity(Payment entity) {
        if (entity == null) {
            return null;
        }

        return PaymentDTO.builder()
                .id(entity.getId())
                .diningSessionId(entity.getDiningSession() != null ? entity.getDiningSession().getId() : null)
                .provider(entity.getProvider())
                .providerReference(entity.getProviderReference())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}