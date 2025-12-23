package com.isysnap.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartItemResponse {
    private String id;
    private String name;
    private BigDecimal unitPrice;
    private Integer quantity;
    private List<String> options;
    private BigDecimal subtotal;
}