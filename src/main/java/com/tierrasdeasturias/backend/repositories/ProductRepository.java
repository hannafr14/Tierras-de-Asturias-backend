package com.tierrasdeasturias.backend.repositories;

import com.tierrasdeasturias.backend.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String search);
    List<Product> findByNameAndFarm_Name(String name, String farmName);
    List<Product> findByFarm_IdOrderByNameAscIdAsc(Long farmId);
}
