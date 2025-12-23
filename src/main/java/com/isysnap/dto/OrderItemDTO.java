package com.isysnap.dto;

import com.isysnap.entity.OrderItem;
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
public class OrderItemDTO {

    private String id;
    private String orderId;       // Solo ID, NO oggetto Order nested
    private String menuItemId;    // Solo ID, NO oggetto MenuItem nested
    private String guestId;       // Solo ID, NO oggetto DiningSessionGuest nested
    private String nameSnapshot;
    private BigDecimal unitPriceSnapshot;
    private Integer quantity;
    private String notes;
    private Instant createdAt;

    public static OrderItemDTO fromEntity(OrderItem entity) {
        if (entity == null) {
            return null;
        }

        return OrderItemDTO.builder()
                .id(entity.getId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .menuItemId(entity.getMenuItem() != null ? entity.getMenuItem().getId() : null)
                .guestId(entity.getGuest() != null ? entity.getGuest().getId() : null)
                .nameSnapshot(entity.getNameSnapshot())
                .unitPriceSnapshot(entity.getUnitPriceSnapshot())
                .quantity(entity.getQuantity())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}