package com.isysnap.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantInfoResponse {
    private String id;
    private String name;
    private String logo;
    private String address;
}