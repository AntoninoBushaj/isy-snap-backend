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
                Permission.ADMIN_READ,
                Permission.ADMIN_CREATE,
                Permission.ADMIN_UPDATE,
                Permission.ADMIN_DELETE,
                Permission.STAFF_READ,
                Permission.STAFF_CREATE,
                Permission.STAFF_UPDATE,
                Permission.STAFF_DELETE,
                Permission.CUSTOMER_READ,
                Permission.CUSTOMER_CREATE
        )),
        STAFF(Set.of(
                Permission.STAFF_READ,
                Permission.STAFF_CREATE,
                Permission.STAFF_UPDATE,
                Permission.STAFF_DELETE,
                Permission.CUSTOMER_READ
        )),
        CUSTOMER(Set.of(
                Permission.CUSTOMER_READ,
                Permission.CUSTOMER_CREATE
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