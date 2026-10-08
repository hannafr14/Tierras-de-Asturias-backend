package com.tierrasdeasturias.backend.controllers;

import com.tierrasdeasturias.backend.dtos.ProductResponseDto;
import com.tierrasdeasturias.backend.services.ProductService;
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

@WebMvcTest(ProductController.class)
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void getAllProductsReturnsProducts() throws Exception {
        ProductResponseDto product = new ProductResponseDto(
                1L,
                "Queso Gamoneu",
                "Queso artesano asturiano",
                "https://example.com/queso.jpg",
                5,
                12.5,
                "Quesos",
                10L,
                "Finca La Vara",
                "Oviedo"
        );

        when(productService.getAllProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Queso Gamoneu"))
                .andExpect(jsonPath("$[0].description").value("Queso artesano asturiano"))
                .andExpect(jsonPath("$[0].image").value("https://example.com/queso.jpg"))
                .andExpect(jsonPath("$[0].quantity").value(5))
                .andExpect(jsonPath("$[0].price").value(12.5))
                .andExpect(jsonPath("$[0].category").value("Quesos"))
                .andExpect(jsonPath("$[0].farmId").value(10))
                .andExpect(jsonPath("$[0].farmName").value("Finca La Vara"))
                .andExpect(jsonPath("$[0].farmAddress").value("Oviedo"));
    }
}
