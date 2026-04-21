package com.isysnap.security;

import com.isysnap.repository.RestaurantUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Used in @PreAuthorize SpEL expressions to enforce restaurant-level data isolation.
 *
 * Usage:
 *   @PreAuthorize("hasAuthority('restaurant:read') and @restaurantAccessValidator.hasAccessToRestaurant(#restaurantId)")
 *
 * ADMINs (who hold 'user:read') bypass the check and have global access.
 * STAFF users must have an active RestaurantUser record for the requested restaurant.
 */
@Component("restaurantAccessValidator")
@RequiredArgsConstructor
public class RestaurantAccessValidator {

    private final RestaurantUserRepository restaurantUserRepository;

    /**
     * Returns true if the current authenticated user has access to the given restaurant.
     */
    public boolean hasAccessToRestaurant(String restaurantId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        // ADMINs hold 'user:read' (a permission not granted to STAFF) — global access
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("user:read"));
        if (isAdmin) {
            return true;
        }

        // STAFF: must have an active RestaurantUser record for this restaurant
        String userId = (String) auth.getPrincipal();
        return restaurantUserRepository.isUserInRestaurant(restaurantId, userId);
    }
}
