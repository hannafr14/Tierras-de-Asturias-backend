package com.tierrasdeasturias.backend.controllers;

import com.tierrasdeasturias.backend.dtos.FarmResponseDto;
import com.tierrasdeasturias.backend.dtos.ProductResponseDto;
import com.tierrasdeasturias.backend.services.FarmService;
import com.tierrasdeasturias.backend.services.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.tierrasdeasturias.backend.config.SecurityConfig;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tierrasdeasturias.backend.config.RememberMeTestConfig;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(FarmController.class)
@Import({SecurityConfig.class, RememberMeTestConfig.class})
public class FarmControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FarmService farmService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getAllFarmsReturnsFarms() throws Exception {
        FarmResponseDto farm = new FarmResponseDto(
                1L,
                "Finca La Vara",
                "Granja familiar asturiana",
                "Oviedo",
                "https://example.com/finca.jpg",
                "https://example.com/finca-banner.jpg",
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
                .andExpect(jsonPath("$[0].bannerImage").value("https://example.com/finca-banner.jpg"))
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
                null,
                43.36,
                -5.84,
                List.of("Quesos", "Miel")
        );

        when(farmService.getAllFarms("vara")).thenReturn(List.of(farm));

        mockMvc.perform(get("/api/farms").param("search", "vara"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Finca La Vara"))
                .andExpect(jsonPath("$[0].bannerImage").value(nullValue()));
    }

    @Test
    void getFarmByIdReturnsFarm() throws Exception {
        FarmResponseDto farm = new FarmResponseDto(
                1L, "Finca La Vara", "Granja familiar",
                "Oviedo", "finca.jpg", "finca-banner.jpg", 43.36, -5.84,
                List.of("Quesos")
        );

        when(farmService.getFarmById(1L)).thenReturn(farm);

        mockMvc.perform(get("/api/farms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Finca La Vara"))
                .andExpect(jsonPath("$.description").value("Granja familiar"))
                .andExpect(jsonPath("$.address").value("Oviedo"))
                .andExpect(jsonPath("$.image").value("finca.jpg"))
                .andExpect(jsonPath("$.bannerImage").value("finca-banner.jpg"))
                .andExpect(jsonPath("$.latitude").value(43.36))
                .andExpect(jsonPath("$.longitude").value(-5.84))
                .andExpect(jsonPath("$.categories[0]").value("Quesos"));
    }

    @Test
    void getFarmByIdReturnsNotFound() throws Exception {
        when(farmService.getFarmById(999L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Granja no encontrada"
                ));

        mockMvc.perform(get("/api/farms/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFarmProductsReturnsProducts() throws Exception {
        when(farmService.getFarmById(1L)).thenReturn(farm());
        when(productService.getProductsByFarmId(1L)).thenReturn(List.of(
                new ProductResponseDto(
                        2L, "Queso", "Artesano", "queso.jpg",
                        5, 12.5, "Quesos", 1L, "Finca La Vara",
                        "Oviedo", 500.0, "g"
                )
        ));

        mockMvc.perform(get("/api/farms/1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].farmId").value(1))
                .andExpect(jsonPath("$[0].sizeValue").value(500.0));
    }

    @Test
    void getFarmProductsReturnsEmptyList() throws Exception {
        when(farmService.getFarmById(1L)).thenReturn(farm());
        when(productService.getProductsByFarmId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/farms/1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getFarmProductsReturnsNotFound() throws Exception {
        when(farmService.getFarmById(999L)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        mockMvc.perform(get("/api/farms/999/products"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(productService);
    }

    private FarmResponseDto farm() {
        return new FarmResponseDto(
                1L, "Finca La Vara", "Granja familiar",
                "Oviedo", "finca.jpg", null, 43.36, -5.84, List.of("Quesos")
        );
    }
}
