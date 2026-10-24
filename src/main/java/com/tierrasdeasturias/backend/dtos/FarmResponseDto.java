package com.tierrasdeasturias.backend.dtos;

import java.util.List;

public record FarmResponseDto(
        Long id,
        String name,
        String description,
        String address,
        String image,
        String bannerImage,
        Double latitude,
        Double longitude,
        List<String> categories
) {
}
