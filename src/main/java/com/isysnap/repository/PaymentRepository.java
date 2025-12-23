package com.isysnap.repository;

import com.isysnap.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    java.util.List<Payment> findByDiningSessionId(String diningSessionId);
}