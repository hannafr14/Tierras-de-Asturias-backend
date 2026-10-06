package com.tierrasdeasturias.backend.repositories;

import com.tierrasdeasturias.backend.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
