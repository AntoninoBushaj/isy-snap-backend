package com.isysnap.scheduled;

import com.isysnap.entity.DiningSession;
import com.isysnap.repository.DiningSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCleanupScheduler {

    private final DiningSessionRepository diningSessionRepository;

    private static final int INACTIVITY_TIMEOUT_MINUTES = 30;

    /**
     * Runs every 10 minutes to mark inactive sessions as ABANDONED.
     * A session is considered abandoned if:
     * - Status is IDLE or ACTIVE
     * - last_activity_at is older than 30 minutes
     */
    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void markAbandonedSessions() {
        log.info("Starting abandoned session cleanup job");

        Instant cutoffTime = Instant.now().minus(INACTIVITY_TIMEOUT_MINUTES, ChronoUnit.MINUTES);

        List<DiningSession> activeSessions = diningSessionRepository
                .findByStatusIn(List.of("IDLE", "ACTIVE"));

        int abandonedCount = 0;

        for (DiningSession session : activeSessions) {
            if (session.getLastActivityAt().isBefore(cutoffTime)) {
                session.setStatus("ABANDONED");
                session.setClosedAt(Instant.now());
                diningSessionRepository.save(session);
                abandonedCount++;

                log.info("Marked session {} as ABANDONED (last activity: {})",
                        session.getId(), session.getLastActivityAt());
            }
        }

        log.info("Abandoned session cleanup completed. Marked {} sessions as ABANDONED", abandonedCount);
    }
}