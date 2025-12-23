package com.isysnap.dto;

import com.isysnap.entity.MenuItemOption;
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
public class MenuItemOptionDTO {

    private String id;
    private String menuItemId;  // Solo ID, NO oggetto MenuItem nested
    private String name;
    private BigDecimal priceDelta;
    private Boolean isMultiple;
    private Instant createdAt;

    public static MenuItemOptionDTO fromEntity(MenuItemOption entity) {
        if (entity == null) {
            return null;
        }

        return MenuItemOptionDTO.builder()
                .id(entity.getId())
                .menuItemId(entity.getMenuItem() != null ? entity.getMenuItem().getId() : null)
                .name(entity.getName())
                .priceDelta(entity.getPriceDelta())
                .isMultiple(entity.getIsMultiple())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}