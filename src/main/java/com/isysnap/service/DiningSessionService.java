package com.isysnap.service;

import com.isysnap.dto.DiningSessionDTO;
import com.isysnap.dto.DiningSessionGuestDTO;
import com.isysnap.dto.response.SessionInfoResponse;
import com.isysnap.entity.DiningSession;
import com.isysnap.entity.DiningSessionGuest;
import com.isysnap.entity.Order;
import com.isysnap.entity.RestaurantTable;
import com.isysnap.repository.DiningSessionGuestRepository;
import com.isysnap.repository.DiningSessionRepository;
import com.isysnap.repository.OrderItemOptionRepository;
import com.isysnap.repository.OrderItemRepository;
import com.isysnap.repository.OrderRepository;
import com.isysnap.repository.OrderStatusHistoryRepository;
import com.isysnap.repository.PaymentItemRepository;
import com.isysnap.repository.PaymentRepository;
import com.isysnap.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiningSessionService {

    private final DiningSessionRepository diningSessionRepository;
    private final DiningSessionGuestRepository guestRepository;
    private final RestaurantTableRepository tableRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentItemRepository paymentItemRepository;

    /**
     * Create a new guest for a session.
     * Each guest represents one person at the table.
     * Guest ID is used in JWT for identification (no separate guest token needed).
     *
     * @param sessionId Session ID (table session)
     * @return Created guest DTO
     */
    @Transactional
    public DiningSessionGuestDTO createGuest(String sessionId) {
        DiningSession session = diningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        // Calculate next guest number
        Integer nextGuestNumber = guestRepository.countByDiningSessionId(session.getId()) + 1;

        // Create new guest
        DiningSessionGuest newGuest = DiningSessionGuest.builder()
                .id(UUID.randomUUID().toString())
                .diningSession(session)
                .guestNumber(nextGuestNumber)
                .build();

        DiningSessionGuest saved = guestRepository.save(newGuest);
        log.info("Created new guest {} with number {} for session {}",
                saved.getId(), nextGuestNumber, session.getId());

        return DiningSessionGuestDTO.fromEntity(saved);
    }

    /**
     * Find guest by ID
     *
     * @param guestId Guest ID
     * @return Guest DTO if found
     */
    @Transactional(readOnly = true)
    public Optional<DiningSessionGuestDTO> findGuestById(String guestId) {
        return guestRepository.findById(guestId)
                .map(DiningSessionGuestDTO::fromEntity);
    }

    /**
     * Find active session by session ID
     */
    @Transactional(readOnly = true)
    public Optional<DiningSessionDTO> findById(String sessionId) {
        return diningSessionRepository.findById(sessionId)
                .map(DiningSessionDTO::fromEntity);
    }

    /**
     * Create a new dining session for a table (called on first item added to cart).
     * Session is created in IDLE status.
     */
    @Transactional
    public DiningSessionDTO createSession(String tableQrSlug) {
        log.info("Creating new dining session for table QR: {}", tableQrSlug);

        RestaurantTable table = tableRepository.findByQrSlug(tableQrSlug)
                .orElseThrow(() -> new RuntimeException("Table not found: " + tableQrSlug));

        // Check if there's already an active session for this table
        Optional<DiningSession> existingSession = diningSessionRepository
                .findByTableIdAndStatusIn(table.getId(), List.of("IDLE", "ACTIVE"))
                .stream()
                .findFirst();

        if (existingSession.isPresent()) {
            log.info("Found existing active session for table: {}", table.getId());
            return DiningSessionDTO.fromEntity(existingSession.get());
        }

        DiningSession session = DiningSession.builder()
                .id(UUID.randomUUID().toString())
                .restaurant(table.getRestaurant())
                .table(table)
                .status("IDLE")
                .build();

        DiningSession saved = diningSessionRepository.save(session);
        log.info("Created new dining session {}", saved.getId());

        return DiningSessionDTO.fromEntity(saved);
    }

    /**
     * Update last activity timestamp for a session
     */
    @Transactional
    public void updateLastActivity(String sessionId) {
        diningSessionRepository.findById(sessionId).ifPresent(session -> {
            session.setLastActivityAt(Instant.now());
            diningSessionRepository.save(session);
        });
    }

    /**
     * Activate session (when first order is confirmed)
     */
    @Transactional
    public void activateSession(String sessionId) {
        diningSessionRepository.findById(sessionId).ifPresent(session -> {
            if ("IDLE".equals(session.getStatus())) {
                session.setStatus("ACTIVE");
                session.setActivatedAt(Instant.now());
                session.setLastActivityAt(Instant.now());
                diningSessionRepository.save(session);
                log.info("Activated session: {}", sessionId);
            }
        });
    }

    /**
     * Close session (when all payments are completed)
     */
    @Transactional
    public void closeSession(String sessionId) {
        diningSessionRepository.findById(sessionId).ifPresent(session -> {
            session.setStatus("CLOSED");
            session.setClosedAt(Instant.now());
            diningSessionRepository.save(session);
            log.info("Closed session: {}", sessionId);
        });
    }

    /**
     * Delete a session and all associated data (cascade)
     */
    @Transactional
    public void deleteSession(String sessionId) {
        log.info("Deleting session: {} — starting cascade cleanup", sessionId);

        DiningSession session = diningSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        // 1. Delete payment_items and payments
        paymentRepository.findByDiningSessionId(sessionId).forEach(payment -> {
            paymentItemRepository.findByPaymentId(payment.getId())
                    .forEach(paymentItemRepository::delete);
            paymentRepository.delete(payment);
        });

        // 2. Delete orders and their children
        List<Order> orders = orderRepository.findByDiningSessionId(sessionId);
        orders.forEach(order -> {
            orderItemOptionRepository.deleteByOrderItemOrderId(order.getId());
            orderStatusHistoryRepository.deleteByOrderId(order.getId());
            orderItemRepository.findByOrderId(order.getId())
                    .forEach(orderItemRepository::delete);
            orderRepository.delete(order);
        });

        // 3. Delete guests
        guestRepository.findByDiningSessionId(sessionId)
                .forEach(guestRepository::delete);

        // 4. Delete session
        diningSessionRepository.delete(session);

        log.info("Deleted session: {} and all associated data", sessionId);
    }

    /**
     * Get all guests for a session
     */
    @Transactional(readOnly = true)
    public List<DiningSessionGuestDTO> getSessionGuests(String sessionId) {
        List<DiningSessionGuest> guests = guestRepository.findByDiningSessionId(sessionId);
        return guests.stream()
                .map(DiningSessionGuestDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all sessions for a specific restaurant (all statuses, ordered by most recent)
     */
    @Transactional(readOnly = true)
    public List<SessionInfoResponse> getSessionsByRestaurant(String restaurantId) {
        log.info("Getting sessions for restaurant: {}", restaurantId);
        List<DiningSession> sessions = diningSessionRepository.findByRestaurantIdOrderByOpenedAtDesc(restaurantId);
        return sessions.stream()
                .map(session -> SessionInfoResponse.builder()
                        .id(session.getId())
                        .status(session.getStatus())
                        .tableCode(session.getTable() != null ? session.getTable().getCode() : "--")
                        .restaurantName(session.getRestaurant() != null ? session.getRestaurant().getName() : "--")
                        .openedAt(session.getOpenedAt())
                        .guestCount(guestRepository.countByDiningSessionId(session.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Get all currently active/idle sessions (ADMIN overview)
     */
    @Transactional(readOnly = true)
    public List<SessionInfoResponse> getAllActiveSessions() {
        log.info("Getting all active sessions");
        List<DiningSession> sessions = diningSessionRepository.findByStatusIn(List.of("IDLE", "ACTIVE"));
        return sessions.stream()
                .map(session -> SessionInfoResponse.builder()
                        .id(session.getId())
                        .status(session.getStatus())
                        .tableCode(session.getTable() != null ? session.getTable().getCode() : "--")
                        .restaurantName(session.getRestaurant() != null ? session.getRestaurant().getName() : "--")
                        .openedAt(session.getOpenedAt())
                        .guestCount(guestRepository.countByDiningSessionId(session.getId()))
                        .build())
                .collect(Collectors.toList());
    }
}