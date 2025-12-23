package com.isysnap.dto;

import com.isysnap.entity.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCategoryDTO {

    private String id;
    private String restaurantId;  // Solo ID, NO oggetto Restaurant nested
    private String name;
    private Integer sortOrder;
    private Instant createdAt;
    private Instant updatedAt;

    public static MenuCategoryDTO fromEntity(MenuCategory entity) {
        if (entity == null) {
            return null;
        }

        return MenuCategoryDTO.builder()
                .id(entity.getId())
                .restaurantId(entity.getRestaurant() != null ? entity.getRestaurant().getId() : null)
                .name(entity.getName())
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}