package com.tierrasdeasturias.backend.repositories;

import com.tierrasdeasturias.backend.entities.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FarmRepository extends JpaRepository<Farm, Long> {
}
