package com.isysnap.repository;

import com.isysnap.entity.DiningSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiningSessionRepository extends JpaRepository<DiningSession, String> {

    List<DiningSession> findByRestaurantIdAndStatus(String restaurantId, String status);

    List<DiningSession> findByRestaurantIdOrderByOpenedAtDesc(String restaurantId);

    List<DiningSession> findByTableIdAndStatus(String tableId, String status);

    List<DiningSession> findByTableIdAndStatusIn(String tableId, List<String> statuses);

    List<DiningSession> findByStatusIn(List<String> statuses);
}