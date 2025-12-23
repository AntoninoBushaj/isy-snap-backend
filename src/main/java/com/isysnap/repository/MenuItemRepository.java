package com.isysnap.repository;

import com.isysnap.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String> {
    List<MenuItem> findByRestaurantIdAndCategoryId(String restaurantId, String categoryId);
    List<MenuItem> findByRestaurantId(String restaurantId);
    List<MenuItem> findByCategoryId(String categoryId);
}