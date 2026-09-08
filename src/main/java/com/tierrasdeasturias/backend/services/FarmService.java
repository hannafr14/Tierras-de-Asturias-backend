package com.tierrasdeasturias.backend.services;

import com.tierrasdeasturias.backend.dtos.FarmResponseDto;
import com.tierrasdeasturias.backend.entities.Farm;
import com.tierrasdeasturias.backend.repositories.FarmRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FarmService {

    private final FarmRepository farmRepository;

    public FarmService(FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
    }

    public List<FarmResponseDto> getAllFarms() {
        return farmRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    private FarmResponseDto mapToResponseDto(Farm farm) {
        List<String> categories = farm.getCategories()
                .stream()
                .map(category -> category.getName())
                .toList();

        return new FarmResponseDto(
                farm.getId(),
                farm.getName(),
                farm.getDescription(),
                farm.getAddress(),
                farm.getImage(),
                farm.getLatitude(),
                farm.getLongitude(),
                categories
        );
    }
}
