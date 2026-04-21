package com.isysnap.controller;

import com.isysnap.dto.DiningSessionDTO;
import com.isysnap.dto.DiningSessionGuestDTO;
import com.isysnap.dto.RestaurantDTO;
import com.isysnap.dto.TableDTO;
import com.isysnap.dto.response.SessionInfoResponse;
import com.isysnap.dto.response.SuccessResponse;
import com.isysnap.service.DiningSessionService;
import com.isysnap.service.RestaurantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Dining session management")
public class SessionController {

    private final DiningSessionService sessionService;
    private final RestaurantService restaurantService;

    @GetMapping("/getSession/{sessionId}")
    public ResponseEntity<SessionInfoResponse> getSession(@PathVariable String sessionId) {
        DiningSessionDTO session = sessionService.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Get table and restaurant info
        TableDTO table = restaurantService.getTableById(session.getTableId());
        RestaurantDTO restaurant = restaurantService.getRestaurantById(session.getRestaurantId());

        return ResponseEntity.ok(SessionInfoResponse.builder()
                .id(session.getId())
                .status(session.getStatus())
                .tableCode(table.getCode())
                .restaurantName(restaurant.getName())
                .openedAt(session.getOpenedAt())
                .build());
    }

    @PatchMapping("/closeSession/{sessionId}")
    @PreAuthorize("hasAuthority('session:update')")
    public ResponseEntity<SuccessResponse> closeSession(@PathVariable String sessionId) {
        sessionService.closeSession(sessionId);
        return ResponseEntity.ok(SuccessResponse.of(true));
    }

    @DeleteMapping("/deleteSession/{sessionId}")
    @PreAuthorize("hasAuthority('session:delete')")
    public ResponseEntity<SuccessResponse> deleteSession(@PathVariable String sessionId) {
        sessionService.deleteSession(sessionId);
        return ResponseEntity.ok(SuccessResponse.of(true));
    }

    @GetMapping("/getRestaurantSessions/{restaurantId}")
    @PreAuthorize("hasAuthority('session:read') and @restaurantAccessValidator.hasAccessToRestaurant(#restaurantId)")
    public ResponseEntity<List<SessionInfoResponse>> getRestaurantSessions(@PathVariable String restaurantId) {
        List<SessionInfoResponse> sessions = sessionService.getSessionsByRestaurant(restaurantId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/getAllActiveSessions")
    @PreAuthorize("hasAuthority('session:read') and hasAuthority('user:read')")
    public ResponseEntity<List<SessionInfoResponse>> getAllActiveSessions() {
        List<SessionInfoResponse> sessions = sessionService.getAllActiveSessions();
        return ResponseEntity.ok(sessions);
    }
}