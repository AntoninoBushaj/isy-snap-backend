package com.isysnap.repository;

import com.isysnap.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    List<OrderItem> findByOrderId(String orderId);

    List<OrderItem> findByGuestId(String guestId);

    List<OrderItem> findByMenuItemId(String menuItemId);
}
