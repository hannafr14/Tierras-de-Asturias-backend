package com.tierrasdeasturias.backend.services;

import com.tierrasdeasturias.backend.dtos.ProductResponseDto;
import com.tierrasdeasturias.backend.entities.Product;
import com.tierrasdeasturias.backend.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponseDto> getAllProducts(String search) {
        List<Product> products;

        if (search == null || search.isBlank()) {
            products = productRepository.findAll();
        } else {
            products = productRepository.findByNameContainingIgnoreCase(search);
        }

        return products.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        return mapToResponseDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getProductsByFarmId(Long farmId) {
        return productRepository.findByFarm_IdOrderByNameAscIdAsc(farmId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    private ProductResponseDto mapToResponseDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getImage(),
                product.getQuantity(),
                product.getPrice(),
                product.getCategory().getName(),
                product.getFarm().getId(),
                product.getFarm().getName(),
                product.getFarm().getAddress(),
                product.getSizeValue(),
                product.getSizeUnit()
        );
    }
}
