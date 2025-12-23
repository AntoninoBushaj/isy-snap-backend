package com.isysnap.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TableInfoResponse {
    private String id;
    private String code;
    private String restaurantId;
}