package com.tierrasdeasturias.backend.services;

import com.tierrasdeasturias.backend.dtos.ProductResponseDto;
import com.tierrasdeasturias.backend.entities.Product;
import com.tierrasdeasturias.backend.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll()
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
                product.getFarm().getName(),
                product.getFarm().getAddress()
        );
    }
}
