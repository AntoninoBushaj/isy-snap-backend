package com.isysnap.controller;

import com.isysnap.dto.OrderDTO;
import com.isysnap.dto.OrderItemDTO;
import com.isysnap.dto.request.AddItemRequest;
import com.isysnap.dto.request.UpdateItemRequest;
import com.isysnap.dto.request.UpdateOrderStatusRequest;
import com.isysnap.dto.response.AddItemResponse;
import com.isysnap.dto.response.CartItemResponse;
import com.isysnap.dto.response.CartResponse;
import com.isysnap.dto.response.SuccessResponse;
import com.isysnap.security.SessionAuthContext;
import com.isysnap.service.DiningSessionService;
import com.isysnap.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order and cart management")
public class OrderController {

    private final OrderService orderService;
    private final DiningSessionService sessionService;

    @PostMapping("/addItemToCart")
    @Operation(summary = "Add item to cart", description = "Add item to cart using guest ID from JWT")
    public ResponseEntity<AddItemResponse> addItemToCart(@RequestBody AddItemRequest request) {
        // Get guest ID and session ID from JWT (populated by SessionGuestAuthenticationFilter)
        String guestId = SessionAuthContext.getGuestId();
        com.isysnap.dto.SessionTokenClaims claims = SessionAuthContext.getSession();

        if (guestId == null || claims == null) {
            throw new RuntimeException("Authentication required: valid session JWT token must be provided");
        }

        String sessionId = claims.getSessionId();

        log.info("Adding item to cart - sessionId: {}, guestId: {}, guest#: {}, menuItemId: {}",
                sessionId, guestId, claims.getGuestNumber(), request.getMenuItemId());

        // Find or create PENDING order for this guest
        OrderDTO order = orderService.findOrCreatePendingOrder(sessionId, guestId);

        // Add item to order
        OrderItemDTO orderItem = orderService.addItemToOrder(
                order.getId(),
                request.getMenuItemId(),
                guestId,
                request.getQuantity(),
                request.getOptionIds(),
                request.getNotes()
        );

        // Update session activity
        sessionService.updateLastActivity(sessionId);

        return ResponseEntity.ok(AddItemResponse.builder()
                .orderItemId(orderItem.getId())
                .orderId(order.getId())
                .itemName(orderItem.getNameSnapshot())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPriceSnapshot())
                .total(orderItem.getUnitPriceSnapshot()
                        .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .build());
    }

    @PatchMapping("/updateCartItem/{orderItemId}")
    @Operation(summary = "Update cart item", description = "Update quantity or remove item")
    public ResponseEntity<SuccessResponse> updateCartItem(
            @PathVariable String orderItemId,
            @RequestBody UpdateItemRequest request) {

        log.info("Updating cart item: {} to quantity: {}", orderItemId, request.getQuantity());

        orderService.updateOrderItemQuantity(orderItemId, request.getQuantity());

        return ResponseEntity.ok(SuccessResponse.of(true));
    }

    @DeleteMapping("/removeCartItem/{orderItemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<SuccessResponse> removeCartItem(@PathVariable String orderItemId) {
        log.info("Removing cart item: {}", orderItemId);

        orderService.removeOrderItem(orderItemId);

        return ResponseEntity.ok(SuccessResponse.of(true));
    }

    @GetMapping("/getCart")
    @Operation(summary = "Get current cart", description = "Returns guest's PENDING order items")
    public ResponseEntity<CartResponse> getCart() {
        // Get guest ID from JWT
        String guestId = com.isysnap.security.SessionAuthContext.getGuestId();
        com.isysnap.dto.SessionTokenClaims claims = com.isysnap.security.SessionAuthContext.getSession();

        if (guestId == null || claims == null) {
            throw new RuntimeException("Authentication required: valid session JWT token must be provided");
        }

        String sessionId = claims.getSessionId();
        log.info("Getting cart - sessionId: {}, guestId: {}", sessionId, guestId);

        // Find PENDING order
        OrderDTO order = orderService.findOrCreatePendingOrder(sessionId, guestId);

        // Get ONLY this guest's order items (not all items in the order)
        List<OrderItemDTO> guestItems = orderService.getOrderItems(order.getId()).stream()
                .filter(item -> item.getGuestId() != null && item.getGuestId().equals(guestId))
                .collect(Collectors.toList());

        // Calculate total based on guest's items only
        BigDecimal totalAmount = guestItems.stream()
                .map(item -> item.getUnitPriceSnapshot()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(CartResponse.builder()
                .orderId(order.getId())
                .items(guestItems.stream()
                        .map(item -> CartItemResponse.builder()
                                .id(item.getId())
                                .name(item.getNameSnapshot())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPriceSnapshot())
                                .subtotal(item.getUnitPriceSnapshot()
                                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                                .build())
                        .collect(Collectors.toList()))
                .total(totalAmount)
                .build());
    }

    @GetMapping("/getOrderHistory")
    @Operation(summary = "Get order history", description = "Returns guest's CONFIRMED orders")
    public ResponseEntity<List<CartResponse>> getOrderHistory() {
        // Get guest ID from JWT
        String guestId = com.isysnap.security.SessionAuthContext.getGuestId();

        if (guestId == null) {
            throw new RuntimeException("Authentication required: valid session JWT token must be provided");
        }

        log.info("Getting order history - guestId: {}", guestId);

        // Get all CONFIRMED orders for this guest
        List<OrderDTO> confirmedOrders = orderService.getConfirmedOrdersByGuest(guestId);

        return ResponseEntity.ok(confirmedOrders.stream()
                .map(order -> {
                    List<OrderItemDTO> orderItems = orderService.getOrderItems(order.getId());
                    BigDecimal totalAmount = orderItems.stream()
                            .map(item -> item.getUnitPriceSnapshot()
                                    .multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return CartResponse.builder()
                            .orderId(order.getId())
                            .items(orderItems.stream()
                                    .map(item -> CartItemResponse.builder()
                                            .id(item.getId())
                                            .name(item.getNameSnapshot())
                                            .quantity(item.getQuantity())
                                            .unitPrice(item.getUnitPriceSnapshot())
                                            .subtotal(item.getUnitPriceSnapshot()
                                                    .multiply(BigDecimal.valueOf(item.getQuantity())))
                                            .build())
                                    .collect(Collectors.toList()))
                            .total(totalAmount)
                            .build();
                })
                .collect(Collectors.toList()));
    }

    // STAFF APIs

    @GetMapping("/getSessionOrders/{sessionId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Get all orders for session", description = "STAFF: View all orders at a table")
    public ResponseEntity<List<CartResponse>> getSessionOrders(@PathVariable String sessionId) {
        log.info("Getting all orders for session: {}", sessionId);

        List<OrderDTO> orders = orderService.getOrdersBySession(sessionId);

        return ResponseEntity.ok(orders.stream()
                .map(order -> {
                    List<OrderItemDTO> orderItems = orderService.getOrderItems(order.getId());
                    BigDecimal totalAmount = orderItems.stream()
                            .map(item -> item.getUnitPriceSnapshot()
                                    .multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return CartResponse.builder()
                            .orderId(order.getId())
                            .items(orderItems.stream()
                                    .map(item -> CartItemResponse.builder()
                                            .id(item.getId())
                                            .name(item.getNameSnapshot())
                                            .quantity(item.getQuantity())
                                            .unitPrice(item.getUnitPriceSnapshot())
                                            .subtotal(item.getUnitPriceSnapshot()
                                                    .multiply(BigDecimal.valueOf(item.getQuantity())))
                                            .build())
                                    .collect(Collectors.toList()))
                            .total(totalAmount)
                            .build();
                })
                .collect(Collectors.toList()));
    }

    @PatchMapping("/updateOrderStatus/{orderId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Update order status", description = "STAFF: Mark order as PREPARING, READY, etc.")
    public ResponseEntity<SuccessResponse> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody UpdateOrderStatusRequest request) {

        log.info("Updating order {} status to: {}", orderId, request.getStatus());

        orderService.updateOrderStatus(orderId, request.getStatus());

        return ResponseEntity.ok(SuccessResponse.of(true));
    }
}
