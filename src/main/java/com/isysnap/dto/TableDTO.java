package com.isysnap.dto;

import com.isysnap.entity.RestaurantTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableDTO {

    private String id;
    private String restaurantId;  // Solo ID, NO oggetto Restaurant nested
    private String code;
    private String qrSlug;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public static TableDTO fromEntity(RestaurantTable entity) {
        if (entity == null) {
            return null;
        }

        return TableDTO.builder()
                .id(entity.getId())
                .restaurantId(entity.getRestaurant() != null ? entity.getRestaurant().getId() : null)
                .code(entity.getCode())
                .qrSlug(entity.getQrSlug())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}