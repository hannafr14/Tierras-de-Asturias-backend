package com.tierrasdeasturias.backend.controllers;

import com.tierrasdeasturias.backend.dtos.ProductResponseDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tierrasdeasturias.backend.config.RememberMeTestConfig;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, RememberMeTestConfig.class})
public class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private UserDetailsService userDetailsService;

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

        when(productService.getAllProducts(null)).thenReturn(List.of(product));

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

    @Test
    void getAllProductsFiltersBySearchQuery() throws Exception {
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

        when(productService.getAllProducts("queso")).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products").param("search", "queso"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Queso Gamoneu"));
    }

    @Test
    void getProductByIdReturnsProduct() throws Exception {
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

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Queso Gamoneu"))
                .andExpect(jsonPath("$.description")
                        .value("Queso artesano asturiano"))
                .andExpect(jsonPath("$.price").value(12.5))
                .andExpect(jsonPath("$.image")
                        .value("https://example.com/queso.jpg"))
                .andExpect(jsonPath("$.farmId").value(10))
                .andExpect(jsonPath("$.farmName").value("Finca La Vara"));
    }

    @Test
    void getProductByIdReturnsNotFound() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }
}
