package com.tierrasdeasturias.backend.services;

import com.tierrasdeasturias.backend.dtos.FarmResponseDto;
import com.tierrasdeasturias.backend.entities.Farm;
import com.tierrasdeasturias.backend.repositories.FarmRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FarmService {

    private final FarmRepository farmRepository;

    public FarmService(FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
    }

    public List<FarmResponseDto> getAllFarms(String search) {
        List<Farm> farms;

        if (search == null || search.isBlank()) {
            farms = farmRepository.findAll();
        } else {
            farms = farmRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                    search,
                    search
            );
        }

        return farms.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public FarmResponseDto getFarmById(Long id) {
        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Granja no encontrada"
                ));

        return mapToResponseDto(farm);
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
                farm.getBannerImage(),
                farm.getLatitude(),
                farm.getLongitude(),
                categories
        );
    }
}
