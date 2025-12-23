package com.isysnap.dto;

import com.isysnap.entity.MenuItem;
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
public class MenuItemDTO {

    private String id;
    private String restaurantId;  // Solo ID, NO oggetto Restaurant nested
    private String categoryId;    // Solo ID, NO oggetto MenuCategory nested
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Boolean isAvailable;
    private Instant createdAt;
    private Instant updatedAt;

    public static MenuItemDTO fromEntity(MenuItem entity) {
        if (entity == null) {
            return null;
        }

        return MenuItemDTO.builder()
                .id(entity.getId())
                .restaurantId(entity.getRestaurant() != null ? entity.getRestaurant().getId() : null)
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .imageUrl(entity.getImageUrl())
                .isAvailable(entity.getIsAvailable())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}