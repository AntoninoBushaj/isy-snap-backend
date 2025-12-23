package com.isysnap.service;

import com.isysnap.dto.OrderDTO;
import com.isysnap.dto.OrderItemDTO;
import com.isysnap.entity.*;
import com.isysnap.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final MenuItemOptionRepository menuItemOptionRepository;
    private final DiningSessionGuestRepository guestRepository;
    private final DiningSessionService diningSessionService;

    /**
     * Create a new order for a dining session
     */
    @Transactional
    public Order createOrder(DiningSession session, String notes) {
        log.info("Creating new order for session: {}", session.getId());

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .diningSession(session)
                .status("PENDING")
                .notes(notes)
                .build();

        Order saved = orderRepository.save(order);

        // Create status history
        createStatusHistory(saved, "PENDING", "Order created");

        // Update session activity
        diningSessionService.updateLastActivity(session.getId());

        log.info("Created order: {}", saved.getId());
        return saved;
    }

    /**
     * Add item to order
     */
    @Transactional
    public OrderItemDTO addItemToOrder(String orderId, String menuItemId, String guestId,
                                     Integer quantity, List<String> optionIds, String notes) {
        log.info("Adding item {} to order {}", menuItemId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + menuItemId));

        DiningSessionGuest guest = guestId != null ?
                guestRepository.findById(guestId)
                        .orElseThrow(() -> new RuntimeException("Guest not found: " + guestId))
                : null;

        // Calculate unit price with options
        BigDecimal unitPrice = menuItem.getPrice();
        if (optionIds != null && !optionIds.isEmpty()) {
            for (String optionId : optionIds) {
                MenuItemOption option = menuItemOptionRepository.findById(optionId)
                        .orElseThrow(() -> new RuntimeException("Option not found: " + optionId));
                unitPrice = unitPrice.add(option.getPriceDelta());
            }
        }

        // Create order item with snapshot
        OrderItem orderItem = OrderItem.builder()
                .id(UUID.randomUUID().toString())
                .order(order)
                .menuItem(menuItem)
                .guest(guest)
                .nameSnapshot(menuItem.getName())
                .unitPriceSnapshot(unitPrice)
                .quantity(quantity)
                .notes(notes)
                .build();

        OrderItem savedItem = orderItemRepository.save(orderItem);

        // Save selected options as snapshots
        if (optionIds != null && !optionIds.isEmpty()) {
            for (String optionId : optionIds) {
                MenuItemOption option = menuItemOptionRepository.findById(optionId)
                        .orElseThrow(() -> new RuntimeException("Option not found: " + optionId));

                OrderItemOption orderItemOption = OrderItemOption.builder()
                        .id(UUID.randomUUID().toString())
                        .orderItem(savedItem)
                        .optionNameSnapshot(option.getName())
                        .priceDeltaSnapshot(option.getPriceDelta())
                        .build();

                orderItemOptionRepository.save(orderItemOption);
            }
        }

        // Update order total
        updateOrderTotal(order);

        // Update session activity
        diningSessionService.updateLastActivity(order.getDiningSession().getId());

        log.info("Added order item: {}", savedItem.getId());
        return OrderItemDTO.fromEntity(savedItem);
    }

    /**
     * Confirm order (transition from PENDING to IN_PREPARATION)
     */
    @Transactional
    public void confirmOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("Only PENDING orders can be confirmed");
        }

        order.setStatus("IN_PREPARATION");
        orderRepository.save(order);

        createStatusHistory(order, "IN_PREPARATION", "Order confirmed and sent to kitchen");

        // Activate session if it's IDLE
        diningSessionService.activateSession(order.getDiningSession().getId());

        log.info("Confirmed order: {}", orderId);
    }

    /**
     * Find or create PENDING order for guest
     */
    @Transactional
    public OrderDTO findOrCreatePendingOrder(String sessionId, String guestId) {
        // Look for existing PENDING order for this specific guest
        List<Order> orders = orderRepository.findByDiningSessionId(sessionId);
        for (Order order : orders) {
            if ("PENDING".equals(order.getStatus())) {
                // Check if this order belongs to the specific guest
                List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

                // If order has items, check if they belong to this guest
                if (!items.isEmpty()) {
                    // If any item belongs to this guest, this is their order
                    boolean belongsToGuest = items.stream()
                            .anyMatch(item -> item.getGuest() != null &&
                                    item.getGuest().getId().equals(guestId));

                    if (belongsToGuest) {
                        log.info("Found existing PENDING order: {} for guest: {}", order.getId(), guestId);
                        return OrderDTO.fromEntity(order);
                    }
                }
                // If order has no items, we can reuse it for this guest
                else {
                    log.info("Found empty PENDING order: {} for guest: {}", order.getId(), guestId);
                    return OrderDTO.fromEntity(order);
                }
            }
        }

        // Create new PENDING order for this guest
        DiningSessionGuest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found: " + guestId));

        log.info("Creating new PENDING order for session: {}, guest: {}", sessionId, guestId);
        Order newOrder = createOrder(guest.getDiningSession(), null);
        return OrderDTO.fromEntity(newOrder);
    }

    /**
     * Update order item quantity
     */
    @Transactional
    public void updateOrderItemQuantity(String orderItemId, Integer newQuantity) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found: " + orderItemId));

        if (newQuantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        orderItem.setQuantity(newQuantity);
        orderItemRepository.save(orderItem);

        // Update order total
        updateOrderTotal(orderItem.getOrder());

        log.info("Updated order item {} quantity to {}", orderItemId, newQuantity);
    }

    /**
     * Remove order item
     */
    @Transactional
    public void removeOrderItem(String orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new RuntimeException("Order item not found: " + orderItemId));

        Order order = orderItem.getOrder();

        // Delete associated options first
        orderItemOptionRepository.deleteByOrderItemId(orderItemId);

        // Delete the order item
        orderItemRepository.delete(orderItem);

        // Update order total
        updateOrderTotal(order);

        log.info("Removed order item: {}", orderItemId);
    }

    /**
     * Get order items for an order
     */
    @Transactional(readOnly = true)
    public List<OrderItemDTO> getOrderItems(String orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return items.stream()
                .map(OrderItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get confirmed orders for a guest
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getConfirmedOrdersByGuest(String guestId) {
        List<Order> allOrders = orderRepository.findAll();
        return allOrders.stream()
                .filter(order -> !"PENDING".equals(order.getStatus()))
                .filter(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return items.stream()
                            .anyMatch(item -> item.getGuest() != null &&
                                    item.getGuest().getId().equals(guestId));
                })
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all orders for a session
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersBySession(String sessionId) {
        List<Order> orders = orderRepository.findByDiningSessionId(sessionId);
        return orders.stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update order status (overload without notes)
     */
    @Transactional
    public void updateOrderStatus(String orderId, String newStatus) {
        updateOrderStatus(orderId, newStatus, null);
    }

    /**
     * Update order status
     */
    @Transactional
    public void updateOrderStatus(String orderId, String newStatus, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        String oldStatus = order.getStatus();
        order.setStatus(newStatus);
        orderRepository.save(order);

        createStatusHistory(order, newStatus, notes != null ? notes : "Status changed from " + oldStatus);

        log.info("Updated order {} status from {} to {}", orderId, oldStatus, newStatus);
    }

    /**
     * Get all orders for a dining session
     */
    @Transactional(readOnly = true)
    public List<OrderDTO> getSessionOrders(String sessionId) {
        List<Order> orders = orderRepository.findByDiningSessionId(sessionId);
        return orders.stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get order items for a specific guest
     */
    @Transactional(readOnly = true)
    public List<OrderItemDTO> getGuestOrderItems(String guestId) {
        List<OrderItem> items = orderItemRepository.findByGuestId(guestId);
        return items.stream()
                .map(OrderItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private void updateOrderTotal(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());

        BigDecimal total = items.stream()
                .map(item -> item.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);
        orderRepository.save(order);

        log.info("Updated order {} total to {}", order.getId(), total);
    }

    private void createStatusHistory(Order order, String status, String notes) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .id(UUID.randomUUID().toString())
                .order(order)
                .status(status)
                .notes(notes)
                .build();

        orderStatusHistoryRepository.save(history);
    }
}
