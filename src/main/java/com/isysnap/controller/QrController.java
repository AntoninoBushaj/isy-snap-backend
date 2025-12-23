package com.isysnap.controller;

import com.isysnap.dto.DiningSessionDTO;
import com.isysnap.dto.DiningSessionGuestDTO;
import com.isysnap.dto.request.QrAuthorizeRequest;
import com.isysnap.dto.response.QrAuthorizeResponse;
import com.isysnap.dto.response.RestaurantInfoResponse;
import com.isysnap.dto.response.TableInfoResponse;
import com.isysnap.entity.RestaurantTable;
import com.isysnap.repository.RestaurantTableRepository;
import com.isysnap.security.SessionTokenProvider;
import com.isysnap.service.DiningSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@Tag(name = "QR Authorization", description = "QR code validation and authorization")
public class QrController {

    private final RestaurantTableRepository tableRepository;
    private final SessionTokenProvider sessionTokenProvider;
    private final DiningSessionService diningSessionService;

    @Value("${qr.secret.key:your-secret-key-change-in-production}")
    private String secretKey;

    private static final long MAX_QR_AGE_SECONDS = 300; // 5 minutes

    @PostMapping("/authorizeQr")
    @Operation(summary = "Authorize QR code", description = "Validates scanned QR code and returns session token")
    public ResponseEntity<QrAuthorizeResponse> authorizeQr(@RequestBody QrAuthorizeRequest request) {
        log.info("QR authorization request for slug: {}", request.getQrSlug());

        // 1. Validate timestamp (freshness check)
        long currentTime = Instant.now().getEpochSecond();
        if (currentTime - request.getTimestamp() > MAX_QR_AGE_SECONDS) {
            log.warn("QR code expired for slug: {}", request.getQrSlug());
            return ResponseEntity.ok(QrAuthorizeResponse.builder()
                    .valid(false)
                    .build());
        }

        // 2. Find table by QR slug
        RestaurantTable table = tableRepository.findByQrSlug(request.getQrSlug())
                .orElse(null);

        if (table == null || !table.getIsActive()) {
            log.warn("Table not found or inactive for slug: {}", request.getQrSlug());
            return ResponseEntity.ok(QrAuthorizeResponse.builder()
                    .valid(false)
                    .build());
        }

        // 3. Validate signature
        String expectedSignature = generateSignature(
                request.getQrSlug(),
                request.getTimestamp(),
                request.getEphemeralKey()
        );

        if (!expectedSignature.equals(request.getSignature())) {
            log.warn("Invalid signature for slug: {}", request.getQrSlug());
            return ResponseEntity.ok(QrAuthorizeResponse.builder()
                    .valid(false)
                    .build());
        }

        // 4. Find or create dining session for this table
        DiningSessionDTO session = diningSessionService.createSession(request.getQrSlug());

        // 5. Create a new guest for this person
        DiningSessionGuestDTO guest = diningSessionService.createGuest(session.getId());

        // 6. Generate session JWT token with guest ID
        String sessionJwt = sessionTokenProvider.generateSessionToken(
                session.getId(),
                guest.getId(),
                table.getId(),
                table.getRestaurant().getId(),
                table.getCode(),
                guest.getGuestNumber(),
                request.getQrSlug()
        );

        log.info("QR authorized successfully, session: {}, guest: {}, guest#: {}, JWT generated",
                session.getId(), guest.getId(), guest.getGuestNumber());

        return ResponseEntity.ok(QrAuthorizeResponse.builder()
                .valid(true)
                .sessionToken(sessionJwt)
                .sessionId(session.getId())
                .tableInfo(TableInfoResponse.builder()
                        .id(table.getId())
                        .code(table.getCode())
                        .restaurantId(table.getRestaurant().getId())
                        .build())
                .restaurantInfo(RestaurantInfoResponse.builder()
                        .id(table.getRestaurant().getId())
                        .name(table.getRestaurant().getName())
                        .logo(table.getRestaurant().getLogo())
                        .address(table.getRestaurant().getAddress())
                        .build())
                .build());
    }

    private String generateSignature(String qrSlug, Long timestamp, String ephemeralKey) {
        try {
            String data = qrSlug + timestamp + ephemeralKey;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            sha256Hmac.init(secretKeySpec);
            byte[] hash = sha256Hmac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Error generating signature", e);
            return "";
        }
    }
}