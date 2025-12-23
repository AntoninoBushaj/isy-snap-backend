package com.isysnap.repository;

import com.isysnap.entity.DiningSessionGuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiningSessionGuestRepository extends JpaRepository<DiningSessionGuest, String> {

    List<DiningSessionGuest> findByDiningSessionId(String diningSessionId);

    List<DiningSessionGuest> findByDiningSessionIdOrderByGuestNumberAsc(String diningSessionId);

    Integer countByDiningSessionId(String diningSessionId);
}