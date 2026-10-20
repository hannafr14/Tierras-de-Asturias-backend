package com.tierrasdeasturias.backend.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailsResponseDto(
        Long id,
        String orderNumber,
        String status,
        LocalDateTime orderDate,
        Double subtotal,
        Double shipping,
        Double taxes,
        Double total,
        List<OrderItemResponseDto> items,
        OrderDeliveryResponseDto delivery
) {
}
