package com.tierrasdeasturias.backend.repositories;

import com.tierrasdeasturias.backend.entities.FarmCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmCategoryRepository extends JpaRepository<FarmCategory, Long> {
}
