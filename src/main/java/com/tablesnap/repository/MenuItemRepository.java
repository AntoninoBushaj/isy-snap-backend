package com.tablesnap.repository;

import com.tablesnap.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String> {
    List<MenuItem> findByRestaurantIdAndCategory(String restaurantId, String category);
    List<MenuItem> findByRestaurantId(String restaurantId);
}