package com.tablesnap.service;

import com.tablesnap.dto.request.OrderItemDTO;
import com.tablesnap.dto.request.OrderRequest;
import com.tablesnap.entity.*;
import com.tablesnap.exception.ItemNotAvailableException;
import com.tablesnap.exception.MenuItemNotFoundException;
import com.tablesnap.exception.OrderNotFoundException;
import com.tablesnap.exception.RestaurantNotFoundException;
import com.tablesnap.repository.MenuItemRepository;
import com.tablesnap.repository.OrderRepository;
import com.tablesnap.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public Order createOrder(OrderRequest request) {
        log.info("Creating order for restaurant: {}", request.getRestaurantId());

        // Validate restaurant exists
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(request.getRestaurantId()));

        // Create order
        Order order = Order.builder()
                .restaurant(restaurant)
                .items(new ArrayList<>())
                .specialInstructions(request.getSpecialInstructions())
                .status(Order.OrderStatus.PENDING)
                .estimatedDeliveryTime(20)
                .build();

        // Add items to order
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemDTO itemDTO : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemDTO.getId())
                    .orElseThrow(() -> new MenuItemNotFoundException(itemDTO.getId()));

            if (!menuItem.getAvailable()) {
                throw new ItemNotAvailableException(menuItem.getName());
            }

            BigDecimal itemSubtotal = menuItem.getPrice()
                    .multiply(new BigDecimal(itemDTO.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(itemDTO.getQuantity())
                    .price(menuItem.getPrice())
                    .subtotal(itemSubtotal)
                    .build();

            orderItems.add(orderItem);
            subtotal = subtotal.add(itemSubtotal);
        }

        // Calculate totals
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.10")); // 10% tax
        BigDecimal total = subtotal.add(tax);

        order.setItems(orderItems);
        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setTotalPrice(total);

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {}", savedOrder.getId());
        return savedOrder;
    }

    public List<Order> findAll() {
        log.info("Finding all orders");
        return orderRepository.findAll();
    }

    public Order findById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public List<Order> findByRestaurantId(String restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }
}