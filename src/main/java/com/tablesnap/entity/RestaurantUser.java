package com.tablesnap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "restaurant_users", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"restaurant_id", "user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRestaurantRole role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = true)
    private Instant removedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public boolean isActive() {
        return removedAt == null;
    }

    public enum UserRestaurantRole {
        OWNER,        // Proprietario del ristorante
        MANAGER,      // Gestore del ristorante
        STAFF,        // Personale del ristorante
        CUSTOMER      // Cliente (viewer del menu)
    }
}
