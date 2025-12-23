package com.isysnap.dto;

import com.isysnap.entity.DiningSessionGuest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiningSessionGuestDTO {

    private String id;
    private String sessionId;  // Solo ID, NO oggetto DiningSession nested
    private Integer guestNumber;
    private String guestName;
    private Instant createdAt;

    public static DiningSessionGuestDTO fromEntity(DiningSessionGuest entity) {
        if (entity == null) {
            return null;
        }

        return DiningSessionGuestDTO.builder()
                .id(entity.getId())
                .sessionId(entity.getDiningSession() != null ? entity.getDiningSession().getId() : null)
                .guestNumber(entity.getGuestNumber())
                .guestName(entity.getGuestName())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}