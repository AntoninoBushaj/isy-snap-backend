package com.tablesnap.service;

import com.tablesnap.entity.Restaurant;
import com.tablesnap.entity.RestaurantUser;
import com.tablesnap.entity.User;
import com.tablesnap.exception.AuthenticationException;
import com.tablesnap.repository.RestaurantRepository;
import com.tablesnap.repository.RestaurantUserRepository;
import com.tablesnap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRestaurantService {

    private final RestaurantUserRepository restaurantUserRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    /**
     * Add a user to a restaurant with a specific role
     */
    @Transactional
    public RestaurantUser addUserToRestaurant(String userId, String restaurantId, RestaurantUser.UserRestaurantRole role) {
        log.info("Adding user {} to restaurant {} with role {}", userId, restaurantId, role);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new AuthenticationException("Restaurant not found"));

        // Check if user is already in the restaurant
        if (restaurantUserRepository.isUserInRestaurant(restaurantId, userId)) {
            throw new AuthenticationException("User is already in this restaurant");
        }

        RestaurantUser restaurantUser = RestaurantUser.builder()
                .user(user)
                .restaurant(restaurant)
                .role(role)
                .build();

        RestaurantUser saved = restaurantUserRepository.save(restaurantUser);
        log.info("User {} added to restaurant {} with role {}", userId, restaurantId, role);
        return saved;
    }

    /**
     * Remove a user from a restaurant (soft delete)
     */
    @Transactional
    public void removeUserFromRestaurant(String userId, String restaurantId) {
        log.info("Removing user {} from restaurant {}", userId, restaurantId);

        RestaurantUser restaurantUser = restaurantUserRepository.findByRestaurantIdAndUserId(restaurantId, userId)
                .orElseThrow(() -> new AuthenticationException("User is not in this restaurant"));

        restaurantUser.setRemovedAt(Instant.now());
        restaurantUserRepository.save(restaurantUser);
        log.info("User {} removed from restaurant {}", userId, restaurantId);
    }

    /**
     * Get all restaurants where a user has active roles
     */
    @Transactional(readOnly = true)
    public List<RestaurantUser> getUserRestaurants(String userId) {
        log.debug("Fetching restaurants for user: {}", userId);
        return restaurantUserRepository.findActiveByUserId(userId);
    }

    /**
     * Get all users in a restaurant
     */
    @Transactional(readOnly = true)
    public List<RestaurantUser> getRestaurantUsers(String restaurantId) {
        log.debug("Fetching users for restaurant: {}", restaurantId);
        return restaurantUserRepository.findActiveByRestaurantId(restaurantId);
    }

    /**
     * Update user role in a restaurant
     */
    @Transactional
    public RestaurantUser updateUserRole(String userId, String restaurantId, RestaurantUser.UserRestaurantRole newRole) {
        log.info("Updating user {} role in restaurant {} to {}", userId, restaurantId, newRole);

        RestaurantUser restaurantUser = restaurantUserRepository.findByRestaurantIdAndUserId(restaurantId, userId)
                .orElseThrow(() -> new AuthenticationException("User is not in this restaurant"));

        restaurantUser.setRole(newRole);
        RestaurantUser updated = restaurantUserRepository.save(restaurantUser);
        log.info("User {} role updated to {} in restaurant {}", userId, newRole, restaurantId);
        return updated;
    }

    /**
     * Check if a user has a specific role in a restaurant
     */
    @Transactional(readOnly = true)
    public boolean userHasRoleInRestaurant(String userId, String restaurantId, RestaurantUser.UserRestaurantRole role) {
        return restaurantUserRepository.findByRestaurantIdAndUserId(restaurantId, userId)
                .map(ru -> ru.getRole() == role)
                .orElse(false);
    }

    /**
     * Check if a user has any of the specified roles in a restaurant
     */
    @Transactional(readOnly = true)
    public boolean userHasAnyRoleInRestaurant(String userId, String restaurantId, List<RestaurantUser.UserRestaurantRole> roles) {
        return restaurantUserRepository.findByRestaurantIdAndUserId(restaurantId, userId)
                .map(ru -> roles.contains(ru.getRole()))
                .orElse(false);
    }

    /**
     * Get all staff members of a restaurant (OWNER, MANAGER, STAFF)
     */
    @Transactional(readOnly = true)
    public List<RestaurantUser> getRestaurantStaff(String restaurantId) {
        List<RestaurantUser.UserRestaurantRole> staffRoles = List.of(
                RestaurantUser.UserRestaurantRole.OWNER,
                RestaurantUser.UserRestaurantRole.MANAGER,
                RestaurantUser.UserRestaurantRole.STAFF
        );
        return restaurantUserRepository.findByRestaurantIdAndRoles(restaurantId, staffRoles);
    }

    /**
     * Get owner of a restaurant
     */
    @Transactional(readOnly = true)
    public RestaurantUser getRestaurantOwner(String restaurantId) {
        List<RestaurantUser> owners = restaurantUserRepository.findByRestaurantIdAndRoles(
                restaurantId,
                List.of(RestaurantUser.UserRestaurantRole.OWNER)
        );

        if (owners.isEmpty()) {
            throw new AuthenticationException("Restaurant has no owner");
        }

        return owners.get(0);
    }
}
