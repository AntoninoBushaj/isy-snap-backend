package com.isysnap.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateMenuItemRequest {
    private String restaurantId;
    private String categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
}