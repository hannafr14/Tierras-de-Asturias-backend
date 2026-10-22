package com.tierrasdeasturias.backend.services;

import com.tierrasdeasturias.backend.dtos.ProductResponseDto;
import com.tierrasdeasturias.backend.entities.Farm;
import com.tierrasdeasturias.backend.entities.Product;
import com.tierrasdeasturias.backend.entities.ProductCategory;
import com.tierrasdeasturias.backend.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {
    @Test
    void getProductByIdReturnsProductDetails() {
        var repository = mock(ProductRepository.class);
        var service = new ProductService(repository);
        var product = mock(Product.class);
        var farm = mock(Farm.class);
        var category = mock(ProductCategory.class);

        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("Queso Gamoneu");
        when(product.getDescription()).thenReturn("Queso artesano");
        when(product.getImage()).thenReturn("queso.jpg");
        when(product.getQuantity()).thenReturn(5);
        when(product.getPrice()).thenReturn(12.5);
        when(product.getCategory()).thenReturn(category);
        when(category.getName()).thenReturn("Quesos");
        when(product.getFarm()).thenReturn(farm);
        when(farm.getId()).thenReturn(10L);
        when(farm.getName()).thenReturn("Finca La Vara");
        when(farm.getAddress()).thenReturn("Oviedo");

        var expected = new ProductResponseDto(
                1L, "Queso Gamoneu", "Queso artesano", "queso.jpg",
                5, 12.5, "Quesos", 10L, "Finca La Vara", "Oviedo"
        );

        assertEquals(expected, service.getProductById(1L));
        verify(repository).findById(1L);
    }

    @Test
    void getProductByIdThrowsNotFoundWhenMissing() {
        var repository = mock(ProductRepository.class);
        var service = new ProductService(repository);
        when(repository.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getProductById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Producto no encontrado", exception.getReason());
        verify(repository).findById(999L);
    }
}
