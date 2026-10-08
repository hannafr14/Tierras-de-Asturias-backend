package com.tierrasdeasturias.backend.repositories;

import com.tierrasdeasturias.backend.entities.Farm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
        String name,
        String address
    );
}
