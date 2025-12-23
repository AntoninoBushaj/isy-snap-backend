package com.isysnap.service;

import com.isysnap.dto.OrderDTO;
import com.isysnap.dto.OrderItemDTO;
import com.isysnap.dto.PaymentDTO;
import com.isysnap.entity.*;
import com.isysnap.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentItemRepository paymentItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final DiningSessionRepository diningSessionRepository;
    private final DiningSessionService diningSessionService;

    /**
     * Create payment for specific order items (split bill)
     */
    @Transactional
    public PaymentDTO createPayment(String sessionId, List<String> orderItemIds,
                                   String provider, String providerReference) {
        log.info("Creating payment for session {} with {} items", sessionId, orderItemIds.size());

        // Validate all order items exist and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (String itemId : orderItemIds) {
            OrderItem item = orderItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Order item not found: " + itemId));

            // Check if item is already paid
            List<PaymentItem> existingPayments = paymentItemRepository.findByOrderItemId(itemId);
            if (!existingPayments.isEmpty()) {
                throw new RuntimeException("Order item " + itemId + " is already paid");
            }

            BigDecimal itemTotal = item.getUnitPriceSnapshot()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // Get session entity (needed to create Payment entity)
        DiningSession session = diningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        // Create payment
        Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .diningSession(session)
                .amount(totalAmount)
                .provider(provider)
                .providerReference(providerReference)
                .status("PENDING")
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Create payment items
        for (String itemId : orderItemIds) {
            OrderItem orderItem = orderItemRepository.findById(itemId).orElseThrow();
            BigDecimal itemAmount = orderItem.getUnitPriceSnapshot()
                    .multiply(BigDecimal.valueOf(orderItem.getQuantity()));

            PaymentItem paymentItem = PaymentItem.builder()
                    .id(UUID.randomUUID().toString())
                    .payment(savedPayment)
                    .orderItem(orderItem)
                    .amount(itemAmount)
                    .build();

            paymentItemRepository.save(paymentItem);
        }

        log.info("Created payment {} for amount {}", savedPayment.getId(), totalAmount);
        return PaymentDTO.fromEntity(savedPayment);
    }

    /**
     * Confirm payment (mark as SUCCESS)
     */
    @Transactional
    public void confirmPayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        if (!"PENDING".equals(payment.getStatus())) {
            throw new RuntimeException("Only PENDING payments can be confirmed");
        }

        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);

        // Check if all order items in the session are paid
        checkAndCloseSession(payment.getDiningSession().getId());

        log.info("Confirmed payment: {}", paymentId);
    }

    /**
     * Fail payment
     */
    @Transactional
    public void failPayment(String paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

        payment.setStatus("FAILED");
        paymentRepository.save(payment);

        log.warn("Failed payment {}: {}", paymentId, reason);
    }

    /**
     * Find payment by ID
     */
    @Transactional(readOnly = true)
    public PaymentDTO findById(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        return PaymentDTO.fromEntity(payment);
    }

    /**
     * Get order for payment
     */
    @Transactional(readOnly = true)
    public OrderDTO getOrderForPayment(String paymentId) {
        // Get first payment item to find the order
        List<PaymentItem> paymentItems = paymentItemRepository.findByPaymentId(paymentId);
        if (paymentItems.isEmpty()) {
            throw new RuntimeException("No payment items found for payment: " + paymentId);
        }

        Order order = paymentItems.get(0).getOrderItem().getOrder();
        return OrderDTO.fromEntity(order);
    }

    /**
     * Get all payments for a session
     */
    @Transactional(readOnly = true)
    public List<PaymentDTO> getSessionPayments(String sessionId) {
        List<Payment> payments = paymentRepository.findByDiningSessionId(sessionId);
        return payments.stream()
                .map(PaymentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get unpaid order items for a guest
     */
    @Transactional(readOnly = true)
    public List<OrderItemDTO> getUnpaidItemsForGuest(String guestId) {
        List<OrderItem> guestItems = orderItemRepository.findByGuestId(guestId);

        return guestItems.stream()
                .filter(item -> {
                    List<PaymentItem> payments = paymentItemRepository.findByOrderItemId(item.getId());
                    return payments.isEmpty();
                })
                .map(OrderItemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Check if all items in session are paid, if so close the session
     */
    private void checkAndCloseSession(String sessionId) {
        // Get all orders for this session
        List<Order> orders = diningSessionRepository.findById(sessionId)
                .map(session -> orderItemRepository.findByOrderId(session.getId()))
                .orElse(List.of())
                .stream()
                .map(OrderItem::getOrder)
                .distinct()
                .toList();

        boolean allPaid = true;
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
            for (OrderItem item : items) {
                List<PaymentItem> payments = paymentItemRepository.findByOrderItemId(item.getId());
                if (payments.isEmpty()) {
                    allPaid = false;
                    break;
                }
            }
            if (!allPaid) break;
        }

        if (allPaid && !orders.isEmpty()) {
            diningSessionService.closeSession(sessionId);
        }
    }
}
