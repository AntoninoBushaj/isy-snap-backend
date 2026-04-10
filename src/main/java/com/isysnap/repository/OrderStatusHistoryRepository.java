package com.isysnap.repository;

import com.isysnap.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, String> {

    List<OrderStatusHistory> findByOrderIdOrderByTimestampAsc(String orderId);

    void deleteByOrderId(String orderId);
}