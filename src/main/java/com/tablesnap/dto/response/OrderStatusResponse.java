package com.tablesnap.dto.response;

import com.tablesnap.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusResponse {
    private String id;
    private String restaurantId;
    private String status;
    private Integer estimatedTime;
    private Integer itemCount;
    private BigDecimal totalPrice;
    private Instant lastUpdate;
    private List<StatusHistory> statusHistory;

    public static OrderStatusResponse from(Order order) {
        return OrderStatusResponse.builder()
                .id(order.getId())
                .restaurantId(order.getRestaurant().getId())
                .status(order.getStatus().name().toLowerCase())
                .estimatedTime(order.getEstimatedDeliveryTime())
                .itemCount(order.getItems().size())
                .totalPrice(order.getTotalPrice())
                .lastUpdate(order.getUpdatedAt())
                .statusHistory(new ArrayList<>()) // Can be populated from audit logs if needed
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatusHistory {
        private String status;
        private Instant timestamp;
    }
}