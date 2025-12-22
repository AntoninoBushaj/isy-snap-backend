package com.tablesnap.dto.response;

import com.tablesnap.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {
    private String id;
    private String name;
    private String description;
    private String image;
    private String logo;
    private String address;
    private String phone;
    private List<String> categories;

    public static RestaurantResponse from(Restaurant restaurant, List<String> categories) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .image(restaurant.getImage())
                .categories(categories)
                .build();
    }
}