package com.isysnap.dto.request;

import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    private String status;
    private String notes;
}