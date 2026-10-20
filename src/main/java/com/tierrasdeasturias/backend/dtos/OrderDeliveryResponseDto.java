package com.tierrasdeasturias.backend.dtos;

public record OrderDeliveryResponseDto(
        String firstName,
        String lastName,
        String city,
        String postalCode,
        String streetAddress,
        String apartment,
        String phone
) {
}
