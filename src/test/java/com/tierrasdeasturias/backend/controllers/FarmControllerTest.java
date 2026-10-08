package com.tierrasdeasturias.backend.controllers;

import com.tierrasdeasturias.backend.dtos.FarmResponseDto;
import com.tierrasdeasturias.backend.services.FarmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FarmController.class)
public class FarmControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FarmService farmService;

    @Test
    void getAllFarmsReturnsFarms() throws Exception {
        FarmResponseDto farm = new FarmResponseDto(
                1L,
                "Finca La Vara",
                "Granja familiar asturiana",
                "Oviedo",
                "https://example.com/finca.jpg",
                43.36,
                -5.84,
                List.of("Quesos", "Miel")
        );

        when(farmService.getAllFarms(null)).thenReturn(List.of(farm));

        mockMvc.perform(get("/api/farms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Finca La Vara"))
                .andExpect(jsonPath("$[0].description").value("Granja familiar asturiana"))
                .andExpect(jsonPath("$[0].address").value("Oviedo"))
                .andExpect(jsonPath("$[0].image").value("https://example.com/finca.jpg"))
                .andExpect(jsonPath("$[0].latitude").value(43.36))
                .andExpect(jsonPath("$[0].longitude").value(-5.84))
                .andExpect(jsonPath("$[0].categories[0]").value("Quesos"))
                .andExpect(jsonPath("$[0].categories[1]").value("Miel"));
    }

    @Test
    void getAllFarmsFiltersBySearchQuery() throws Exception {
        FarmResponseDto farm = new FarmResponseDto(
                1L,
                "Finca La Vara",
                "Granja familiar asturiana",
                "Oviedo",
                "https://example.com/finca.jpg",
                43.36,
                -5.84,
                List.of("Quesos", "Miel")
        );

        when(farmService.getAllFarms("vara")).thenReturn(List.of(farm));

        mockMvc.perform(get("/api/farms").param("search", "vara"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Finca La Vara"));
    }
}
