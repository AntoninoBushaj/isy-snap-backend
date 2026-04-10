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
    private String tableCode;        // Codice tavolo (via session → table)
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal total;        // Alias di totalAmount per il frontend
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrderDTO fromEntity(Order entity) {
        if (entity == null) {
            return null;
        }

        String tableCode = null;
        if (entity.getDiningSession() != null && entity.getDiningSession().getTable() != null) {
            tableCode = entity.getDiningSession().getTable().getCode();
        }

        return OrderDTO.builder()
                .id(entity.getId())
                .diningSessionId(entity.getDiningSession() != null ? entity.getDiningSession().getId() : null)
                .tableCode(tableCode)
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .total(entity.getTotalAmount())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}