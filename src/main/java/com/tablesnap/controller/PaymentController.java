package com.tablesnap.controller;

import com.tablesnap.dto.request.PaymentRequest;
import com.tablesnap.dto.response.PaymentResponse;
import com.tablesnap.entity.Payment;
import com.tablesnap.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/processPayment")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        Payment payment = paymentService.processPayment(request);

        PaymentResponse response = PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .method(payment.getMethod().toString())
                .status(payment.getStatus().toString())
                .transactionId(payment.getTransactionId())
                .processedAt(payment.getProcessedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}