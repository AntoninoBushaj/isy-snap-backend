package com.tablesnap.repository;

import com.tablesnap.entity.RestaurantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantUserRepository extends JpaRepository<RestaurantUser, String> {

    /**
     * Find all active restaurant users for a specific restaurant
     */
    @Query("SELECT ru FROM RestaurantUser ru WHERE ru.restaurant.id = :restaurantId AND ru.removedAt IS NULL")
    List<RestaurantUser> findActiveByRestaurantId(@Param("restaurantId") String restaurantId);

    /**
     * Find all active restaurants for a specific user
     */
    @Query("SELECT ru FROM RestaurantUser ru WHERE ru.user.id = :userId AND ru.removedAt IS NULL")
    List<RestaurantUser> findActiveByUserId(@Param("userId") String userId);

    /**
     * Find a specific user-restaurant relationship
     */
    @Query("SELECT ru FROM RestaurantUser ru WHERE ru.restaurant.id = :restaurantId AND ru.user.id = :userId AND ru.removedAt IS NULL")
    Optional<RestaurantUser> findByRestaurantIdAndUserId(@Param("restaurantId") String restaurantId, @Param("userId") String userId);

    /**
     * Check if a user has any active role in a restaurant
     */
    @Query("SELECT CASE WHEN COUNT(ru) > 0 THEN true ELSE false END FROM RestaurantUser ru WHERE ru.restaurant.id = :restaurantId AND ru.user.id = :userId AND ru.removedAt IS NULL")
    boolean isUserInRestaurant(@Param("restaurantId") String restaurantId, @Param("userId") String userId);

    /**
     * Find all users in a restaurant (by their roles)
     */
    @Query("SELECT ru FROM RestaurantUser ru WHERE ru.restaurant.id = :restaurantId AND ru.removedAt IS NULL AND ru.role IN :roles")
    List<RestaurantUser> findByRestaurantIdAndRoles(@Param("restaurantId") String restaurantId, @Param("roles") List<RestaurantUser.UserRestaurantRole> roles);
}
