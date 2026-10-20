package com.tierrasdeasturias.backend.dtos;

public record OrderItemResponseDto(
        Long productId,
        String name,
        String image,
        Integer quantity,
        Double unitPrice
) {
}
