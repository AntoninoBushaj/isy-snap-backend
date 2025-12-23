package com.isysnap.security;

import com.isysnap.dto.DiningSessionGuestDTO;
import com.isysnap.dto.SessionTokenClaims;

/**
 * ThreadLocal context to store session and guest authentication information
 * during HTTP request processing. This allows controllers and services to
 * access authenticated session and guest data without passing it explicitly.
 */
public class SessionAuthContext {

    private static final ThreadLocal<SessionTokenClaims> sessionContext = new ThreadLocal<>();
    private static final ThreadLocal<DiningSessionGuestDTO> guestContext = new ThreadLocal<>();

    /**
     * Set the session claims for the current thread
     */
    public static void setSession(SessionTokenClaims claims) {
        sessionContext.set(claims);
    }

    /**
     * Get the session claims for the current thread
     * @return SessionTokenClaims or null if not set
     */
    public static SessionTokenClaims getSession() {
        return sessionContext.get();
    }

    /**
     * Get guest ID from session context (JWT claims)
     * @return Guest ID or null if not set
     */
    public static String getGuestId() {
        SessionTokenClaims claims = sessionContext.get();
        return claims != null ? claims.getGuestId() : null;
    }

    /**
     * Set the guest information for the current thread
     */
    public static void setGuest(DiningSessionGuestDTO guest) {
        guestContext.set(guest);
    }

    /**
     * Get the guest information for the current thread
     * @return DiningSessionGuestDTO or null if not set
     */
    public static DiningSessionGuestDTO getGuest() {
        return guestContext.get();
    }

    /**
     * Clear all authentication context for the current thread
     * MUST be called after request processing to prevent memory leaks
     */
    public static void clear() {
        sessionContext.remove();
        guestContext.remove();
    }

    /**
     * Check if session authentication is set
     */
    public static boolean hasSession() {
        return sessionContext.get() != null;
    }

    /**
     * Check if guest authentication is set
     */
    public static boolean hasGuest() {
        return guestContext.get() != null;
    }
}