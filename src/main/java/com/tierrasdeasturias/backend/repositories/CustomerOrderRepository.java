package com.tierrasdeasturias.backend.repositories;

import com.tierrasdeasturias.backend.entities.CustomerOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    Page<CustomerOrder> findByUser_EmailOrderByOrderDateDescIdDesc(
            String email,
            Pageable pageable
    );
}
