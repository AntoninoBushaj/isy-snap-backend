package com.isysnap.util;

import com.isysnap.exception.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for accessing security context information
 */
@Slf4j
public class SecurityUtils {

    /**
     * Get the currently authenticated user ID from SecurityContext
     * @return userId of the authenticated user
     * @throws AuthenticationException if user is not authenticated
     */
    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("User not authenticated");
        }

        // The principal contains the userId (set in JwtAuthenticationFilter)
        Object principal = authentication.getPrincipal();

        if (principal instanceof String) {
            return (String) principal;
        }

        throw new AuthenticationException("Invalid authentication principal");
    }

    /**
     * Get the role of the currently authenticated user
     * @return the user's role (e.g., "ADMIN", "STAFF", "CUSTOMER")
     * @throws AuthenticationException if user is not authenticated
     */
    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("User not authenticated");
        }

        return authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))  // Remove "ROLE_" prefix
                .orElseThrow(() -> new AuthenticationException("No role found for user"));
    }

    /**
     * Check if the current user has a specific role
     * @param role the role to check (e.g., "ADMIN", "STAFF", "CUSTOMER")
     * @return true if user has the role, false otherwise
     */
    public static boolean hasRole(String role) {
        try {
            String currentRole = getCurrentUserRole();
            return currentRole.equals(role);
        } catch (AuthenticationException e) {
            return false;
        }
    }

    /**
     * Check if the current user is an ADMIN
     * @return true if user is ADMIN, false otherwise
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if the current user is STAFF
     * @return true if user is STAFF, false otherwise
     */
    public static boolean isStaff() {
        return hasRole("STAFF");
    }

    /**
     * Check if the current user is a CUSTOMER
     * @return true if user is CUSTOMER, false otherwise
     */
    public static boolean isCustomer() {
        return hasRole("CUSTOMER");
    }

    /**
     * Check if there is an authenticated user in the context
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
