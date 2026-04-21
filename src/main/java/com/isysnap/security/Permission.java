package com.isysnap.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {

    // ── User management (ADMIN only) ──────────────────────────────────────────
    USER_READ("user:read"),
    USER_CREATE("user:create"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),

    // ── Restaurant management ─────────────────────────────────────────────────
    RESTAURANT_READ("restaurant:read"),
    RESTAURANT_CREATE("restaurant:create"),
    RESTAURANT_UPDATE("restaurant:update"),
    RESTAURANT_DELETE("restaurant:delete"),

    // ── Menu management ───────────────────────────────────────────────────────
    MENU_READ("menu:read"),
    MENU_CREATE("menu:create"),
    MENU_UPDATE("menu:update"),
    MENU_DELETE("menu:delete"),

    // ── Order management ──────────────────────────────────────────────────────
    ORDER_READ("order:read"),
    ORDER_CREATE("order:create"),
    ORDER_UPDATE("order:update"),
    ORDER_DELETE("order:delete"),

    // ── Session management ────────────────────────────────────────────────────
    SESSION_READ("session:read"),
    SESSION_CREATE("session:create"),
    SESSION_UPDATE("session:update"),
    SESSION_DELETE("session:delete"),

    // ── Payment management ────────────────────────────────────────────────────
    PAYMENT_READ("payment:read"),
    PAYMENT_CREATE("payment:create"),
    PAYMENT_REFUND("payment:refund");

    private final String permission;
}
