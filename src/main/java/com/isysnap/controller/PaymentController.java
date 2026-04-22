package com.isysnap.controller;

import com.isysnap.dto.DiningSessionGuestDTO;
import com.isysnap.dto.OrderDTO;
import com.isysnap.dto.PaymentDTO;
import com.isysnap.dto.request.CheckoutRequest;
import com.isysnap.dto.response.CheckoutResponse;
import com.isysnap.dto.response.PaymentStatusResponse;
import com.isysnap.service.DiningSessionService;
import com.isysnap.service.OrderService;
import com.isysnap.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment and checkout management")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final DiningSessionService sessionService;

    @PostMapping("/createCheckout")
    @Operation(summary = "Checkout cart", description = "Creates payment for entire PENDING order")
    public ResponseEntity<CheckoutResponse> createCheckout(@Valid @RequestBody CheckoutRequest request) {
        // Get guest ID from JWT
        String guestId = com.isysnap.security.SessionAuthContext.getGuestId();
        com.isysnap.dto.SessionTokenClaims claims = com.isysnap.security.SessionAuthContext.getSession();

        if (guestId == null || claims == null) {
            throw new RuntimeException("Authentication required: valid session JWT token must be provided");
        }

        String sessionId = claims.getSessionId();
        log.info("Checkout request - sessionId: {}, guestId: {}, provider: {}",
                sessionId, guestId, request.getProvider());

        // Get all order items from guest's PENDING order
        OrderDTO order = orderService.findOrCreatePendingOrder(sessionId, guestId);

        // Create payment for all items in order
        PaymentDTO payment = paymentService.createPayment(
                sessionId,
                orderService.getOrderItems(order.getId()).stream()
                        .map(item -> item.getId())
                        .toList(),
                request.getProvider(),
                null // providerReference will be set by webhook
        );

        // Generate payment provider URL (mock for now - should integrate with Stripe/PayPal)
        String providerUrl = generatePaymentUrl(payment, request.getProvider());

        return ResponseEntity.ok(CheckoutResponse.builder()
                .paymentId(payment.getId())
                .amount(payment.getAmount())
                .providerUrl(providerUrl)
                .orderId(order.getId())
                .build());
    }

    @GetMapping("/getPaymentStatus/{paymentId}")
    @Operation(summary = "Get payment status", description = "Poll payment status (for frontend)")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(@PathVariable String paymentId) {
        log.info("Getting payment status for: {}", paymentId);

        PaymentDTO payment = paymentService.findById(paymentId);

        // Get associated order (via payment_items)
        OrderDTO order = paymentService.getOrderForPayment(paymentId);

        return ResponseEntity.ok(PaymentStatusResponse.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus())
                .order(PaymentStatusResponse.OrderStatusResponse.builder()
                        .status(order.getStatus())
                        .confirmedAt(order.getCreatedAt())
                        .build())
                .build());
    }

    @PostMapping("/processWebhook/{provider}")
    @Operation(summary = "Payment webhook", description = "Receives payment confirmation from provider")
    public ResponseEntity<Void> processWebhook(
            @PathVariable String provider,
            @RequestBody String webhookPayload) {

        log.info("Received webhook from provider: {}", provider);

        // TODO: Validate webhook signature
        // TODO: Parse webhook payload based on provider
        // TODO: Extract payment ID and status

        // For now, mock the webhook processing
        // In production, you would:
        // 1. Validate the webhook signature (Stripe/PayPal specific)
        // 2. Parse the payload to extract payment info
        // 3. Update payment status
        // 4. Confirm the order if payment successful

        log.warn("Webhook processing not fully implemented - requires Stripe/PayPal integration");

        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirmPayment/{paymentId}")
    @Operation(summary = "Confirm payment", description = "Manual payment confirmation (for testing)")
    public ResponseEntity<Void> confirmPayment(@PathVariable String paymentId) {
        log.info("Manual payment confirmation for: {}", paymentId);

        paymentService.confirmPayment(paymentId);

        return ResponseEntity.ok().build();
    }

    private String generatePaymentUrl(PaymentDTO payment, String provider) {
        // TODO: Integrate with actual payment providers
        // For Stripe: Create checkout session and return session.url
        // For PayPal: Create order and return approval URL

        String baseUrl = "http://localhost:3000"; // Frontend URL

        switch (provider.toLowerCase()) {
            case "stripe":
                // Mock Stripe URL
                return String.format("https://checkout.stripe.com/pay/%s", payment.getId());

            case "paypal":
                // Mock PayPal URL
                return String.format("https://www.paypal.com/checkoutnow?token=%s", payment.getId());

            case "mock":
                // Local testing URL
                return String.format("%s/payment/mock/%s", baseUrl, payment.getId());

            default:
                throw new RuntimeException("Unsupported payment provider: " + provider);
        }
    }
}
