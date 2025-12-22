package com.tablesnap.service;

import com.tablesnap.dto.request.PaymentRequest;
import com.tablesnap.entity.Order;
import com.tablesnap.entity.Payment;
import com.tablesnap.exception.OrderNotFoundException;
import com.tablesnap.exception.PaymentProcessingException;
import com.tablesnap.repository.OrderRepository;
import com.tablesnap.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Payment processPayment(PaymentRequest request) {
        log.info("Processing payment for order: {}", request.getOrderId());

        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(request.getOrderId()));

        // Validate order is in PENDING state
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new PaymentProcessingException("Order is not in PENDING state");
        }

        // Validate payment amount matches order total
        if (!order.getTotalPrice().equals(request.getAmount())) {
            throw new PaymentProcessingException("Payment amount does not match order total");
        }

        // Create payment record
        Payment payment = Payment.builder()
                .order(order)
                .amount(request.getAmount())
                .method(Payment.PaymentMethod.valueOf(request.getMethod().toUpperCase()))
                .status(Payment.PaymentStatus.PROCESSING)
                .build();

        // Simulate payment processing
        try {
            // In a real application, this would integrate with a payment gateway
            // For now, we'll simulate a successful payment
            Thread.sleep(500); // Simulate processing delay

            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setTransactionId("txn-" + UUID.randomUUID().toString().substring(0, 13));
            payment.setProcessedAt(Instant.now());

            // Update order status
            order.setStatus(Order.OrderStatus.CONFIRMED);
            orderRepository.save(order);

            Payment savedPayment = paymentRepository.save(payment);
            log.info("Payment processed successfully: {}", savedPayment.getId());
            return savedPayment;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentProcessingException("Payment processing was interrupted");
        } catch (Exception e) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.error("Payment processing failed", e);
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage());
        }
    }

    public Payment findById(String paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentProcessingException("Payment not found: " + paymentId));
    }

    public Payment findByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentProcessingException("Payment not found for order: " + orderId));
    }
}
