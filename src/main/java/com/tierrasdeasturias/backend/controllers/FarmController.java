package com.tierrasdeasturias.backend.controllers;

import com.tierrasdeasturias.backend.dtos.FarmResponseDto;
import com.tierrasdeasturias.backend.services.FarmService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/farms")
public class FarmController {

    private final FarmService farmService;

    public FarmController(FarmService farmService) {
        this.farmService = farmService;
    }

    @GetMapping
    public List<FarmResponseDto> getAllFarms(
            @RequestParam(required = false) String search
    ) {
        return farmService.getAllFarms(search);
    }

    @GetMapping("/{id}")
    public FarmResponseDto getFarmById(@PathVariable Long id) {
        return farmService.getFarmById(id);
    }
}
