package com.tierrasdeasturias.backend.dtos;

public record ProductResponseDto(
        Long id,
        String name,
        String description,
        String image,
        Integer quantity,
        Double price,
        String category,
        String farmName,
        String farmAddress
) {
}
