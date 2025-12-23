package com.isysnap.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private String orderId;
    private List<CartItemResponse> items;
    private BigDecimal total;
}