package com.tierrasdeasturias.backend.repositories;

import com.tierrasdeasturias.backend.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
