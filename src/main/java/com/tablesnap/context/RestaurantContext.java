package com.tablesnap.context;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestaurantContext {

    private static final ThreadLocal<String> restaurantIdHolder = new ThreadLocal<>();

    /**
     * Set the current restaurant ID for the request context
     * @param restaurantId the restaurant ID to set
     */
    public static void setRestaurantId(String restaurantId) {
        if (restaurantId != null) {
            restaurantIdHolder.set(restaurantId);
            log.debug("RestaurantContext set to: {}", restaurantId);
        }
    }

    /**
     * Get the current restaurant ID from the context
     * @return the current restaurant ID, or null if not set
     */
    public static String getRestaurantId() {
        return restaurantIdHolder.get();
    }

    /**
     * Check if a restaurant context is currently set
     * @return true if restaurant ID is set, false otherwise
     */
    public static boolean isSet() {
        return restaurantIdHolder.get() != null;
    }

    /**
     * Clear the restaurant context (should be called in finally block or filter cleanup)
     */
    public static void clear() {
        String removed = restaurantIdHolder.get();
        restaurantIdHolder.remove();
        if (removed != null) {
            log.debug("RestaurantContext cleared (was: {})", removed);
        }
    }
}