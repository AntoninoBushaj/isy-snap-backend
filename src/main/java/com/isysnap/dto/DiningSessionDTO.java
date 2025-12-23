package com.isysnap.dto;

import com.isysnap.entity.DiningSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiningSessionDTO {

    private String id;
    private String restaurantId;  // Solo ID, NO oggetto Restaurant nested
    private String tableId;       // Solo ID, NO oggetto RestaurantTable nested
    private String status;
    private Instant openedAt;
    private Instant activatedAt;
    private Instant lastActivityAt;
    private Instant closedAt;

    public static DiningSessionDTO fromEntity(DiningSession entity) {
        if (entity == null) {
            return null;
        }

        return DiningSessionDTO.builder()
                .id(entity.getId())
                .restaurantId(entity.getRestaurant() != null ? entity.getRestaurant().getId() : null)
                .tableId(entity.getTable() != null ? entity.getTable().getId() : null)
                .status(entity.getStatus())
                .openedAt(entity.getOpenedAt())
                .activatedAt(entity.getActivatedAt())
                .lastActivityAt(entity.getLastActivityAt())
                .closedAt(entity.getClosedAt())
                .build();
    }
}