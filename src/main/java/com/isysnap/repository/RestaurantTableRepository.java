package com.isysnap.repository;

import com.isysnap.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, String> {

    List<RestaurantTable> findByRestaurantId(String restaurantId);

    Optional<RestaurantTable> findByQrSlug(String qrSlug);

    List<RestaurantTable> findByRestaurantIdAndIsActiveTrue(String restaurantId);
}