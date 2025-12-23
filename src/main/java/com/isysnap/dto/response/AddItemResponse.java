package com.isysnap.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AddItemResponse {
    private String orderItemId;
    private String orderId;
    private String itemName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal total;
}