package com.tierrasdeasturias.backend.repositories;

import com.tierrasdeasturias.backend.entities.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
}
