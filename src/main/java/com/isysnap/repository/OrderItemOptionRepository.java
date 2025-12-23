package com.isysnap.repository;

import com.isysnap.entity.OrderItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemOptionRepository extends JpaRepository<OrderItemOption, String> {

    List<OrderItemOption> findByOrderItemId(String orderItemId);

    void deleteByOrderItemId(String orderItemId);
}