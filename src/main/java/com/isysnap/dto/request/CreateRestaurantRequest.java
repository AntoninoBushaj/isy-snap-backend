package com.isysnap.dto.request;

import lombok.Data;

@Data
public class CreateRestaurantRequest {
    private String name;
    private String description;
    private String address;
    private String phone;
    private String logo;
}