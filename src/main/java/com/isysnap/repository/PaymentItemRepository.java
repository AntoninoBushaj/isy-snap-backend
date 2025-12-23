package com.isysnap.repository;

import com.isysnap.entity.PaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentItemRepository extends JpaRepository<PaymentItem, String> {

    List<PaymentItem> findByPaymentId(String paymentId);

    List<PaymentItem> findByOrderItemId(String orderItemId);
}