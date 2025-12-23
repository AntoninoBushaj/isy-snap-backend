package com.isysnap.repository;

import com.isysnap.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, String> {

    List<MenuCategory> findByRestaurantIdOrderBySortOrderAsc(String restaurantId);
}