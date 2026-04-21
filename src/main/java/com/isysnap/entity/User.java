package com.isysnap.entity;

import com.isysnap.security.Permission;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private String firstName;

    @Column(nullable = true)
    private String lastName;

    @Column(nullable = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant createdAt;

    @Column(nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant updatedAt;

    @Column(columnDefinition = "TIMESTAMP(6)")
    private Instant lastLogin;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public enum UserRole {
        ADMIN(Set.of(
                // Full user management
                Permission.USER_READ, Permission.USER_CREATE,
                Permission.USER_UPDATE, Permission.USER_DELETE,
                // Full restaurant management
                Permission.RESTAURANT_READ, Permission.RESTAURANT_CREATE,
                Permission.RESTAURANT_UPDATE, Permission.RESTAURANT_DELETE,
                // Full menu management
                Permission.MENU_READ, Permission.MENU_CREATE,
                Permission.MENU_UPDATE, Permission.MENU_DELETE,
                // Full order management
                Permission.ORDER_READ, Permission.ORDER_CREATE,
                Permission.ORDER_UPDATE, Permission.ORDER_DELETE,
                // Full session management
                Permission.SESSION_READ, Permission.SESSION_CREATE,
                Permission.SESSION_UPDATE, Permission.SESSION_DELETE,
                // Full payment management (including refunds)
                Permission.PAYMENT_READ, Permission.PAYMENT_CREATE,
                Permission.PAYMENT_REFUND
        )),
        STAFF(Set.of(
                // Restaurant: read + update only (no create/delete)
                Permission.RESTAURANT_READ, Permission.RESTAURANT_UPDATE,
                // Menu: read, create, update (no delete — only ADMIN can remove items permanently)
                Permission.MENU_READ, Permission.MENU_CREATE, Permission.MENU_UPDATE,
                // Full order management
                Permission.ORDER_READ, Permission.ORDER_CREATE,
                Permission.ORDER_UPDATE, Permission.ORDER_DELETE,
                // Session: read + update (can close sessions, cannot delete)
                Permission.SESSION_READ, Permission.SESSION_UPDATE,
                // Payment: read only
                Permission.PAYMENT_READ
        )),
        CUSTOMER(Set.of(
                // Customers only read menu; orders/payments handled via guest session JWT
                Permission.MENU_READ
        ));

        private final Set<Permission> permissions;

        UserRole(Set<Permission> permissions) {
            this.permissions = permissions;
        }

        public Set<Permission> getPermissions() {
            return permissions;
        }

        public List<SimpleGrantedAuthority> getAuthorities() {
            List<SimpleGrantedAuthority> authorities = getPermissions()
                    .stream()
                    .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                    .collect(Collectors.toList());
            authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
            return authorities;
        }
    }
}