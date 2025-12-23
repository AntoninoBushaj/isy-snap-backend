package com.isysnap.dto;

import com.isysnap.entity.OrderItemOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemOptionDTO {

    private String id;
    private String orderItemId;  // Solo ID, NO oggetto OrderItem nested
    private String optionNameSnapshot;
    private BigDecimal priceDeltaSnapshot;

    public static OrderItemOptionDTO fromEntity(OrderItemOption entity) {
        if (entity == null) {
            return null;
        }

        return OrderItemOptionDTO.builder()
                .id(entity.getId())
                .orderItemId(entity.getOrderItem() != null ? entity.getOrderItem().getId() : null)
                .optionNameSnapshot(entity.getOptionNameSnapshot())
                .priceDeltaSnapshot(entity.getPriceDeltaSnapshot())
                .build();
    }
}