package com.isysnap.dto;

import com.isysnap.entity.Order;
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
public class OrderDTO {

    private String id;
    private String diningSessionId;  // Solo ID, NO oggetto DiningSession nested
    private String status;
    private BigDecimal totalAmount;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrderDTO fromEntity(Order entity) {
        if (entity == null) {
            return null;
        }

        return OrderDTO.builder()
                .id(entity.getId())
                .diningSessionId(entity.getDiningSession() != null ? entity.getDiningSession().getId() : null)
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}