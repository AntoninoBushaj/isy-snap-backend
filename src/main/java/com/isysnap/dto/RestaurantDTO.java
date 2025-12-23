package com.isysnap.dto;

import com.isysnap.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantDTO {

    private String id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String logo;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static RestaurantDTO fromEntity(Restaurant entity) {
        if (entity == null) {
            return null;
        }

        return RestaurantDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .address(entity.getAddress())
                .phone(entity.getPhone())
                .logo(entity.getLogo())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}