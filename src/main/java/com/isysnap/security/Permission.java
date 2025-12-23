package com.isysnap.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {

    // Admin permissions
    ADMIN_READ("admin:read"),
    ADMIN_CREATE("admin:create"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_DELETE("admin:delete"),

    // Staff permissions
    STAFF_READ("staff:read"),
    STAFF_CREATE("staff:create"),
    STAFF_UPDATE("staff:update"),
    STAFF_DELETE("staff:delete"),

    // Customer permissions
    CUSTOMER_READ("customer:read"),
    CUSTOMER_CREATE("customer:create");

    private final String permission;
}
