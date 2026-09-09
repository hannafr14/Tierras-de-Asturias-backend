package com.tierrasdeasturias.backend.repositories;

import com.tierrasdeasturias.backend.entities.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
}
