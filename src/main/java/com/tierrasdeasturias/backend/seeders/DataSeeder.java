package com.tierrasdeasturias.backend.seeders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tierrasdeasturias.backend.entities.Farm;
import com.tierrasdeasturias.backend.entities.FarmCategory;
import com.tierrasdeasturias.backend.entities.Product;
import com.tierrasdeasturias.backend.entities.ProductCategory;
import com.tierrasdeasturias.backend.repositories.FarmCategoryRepository;
import com.tierrasdeasturias.backend.repositories.FarmRepository;
import com.tierrasdeasturias.backend.repositories.ProductCategoryRepository;
import com.tierrasdeasturias.backend.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private final FarmRepository farmRepository;
    private final FarmCategoryRepository farmCategoryRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DataSeeder(
            FarmRepository farmRepository,
            FarmCategoryRepository farmCategoryRepository,
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository
    ) {
        this.farmRepository = farmRepository;
        this.farmCategoryRepository = farmCategoryRepository;
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // if (farmRepository.count() > 0 || productRepository.count() > 1) {
        //     return;
        // }

        productRepository.deleteAll();
        productCategoryRepository.deleteAll();
        farmRepository.deleteAll();
        farmCategoryRepository.deleteAll();

        List<FarmSeed> farmSeeds = readJsonFile(
                "data/farmers.json",
                new TypeReference<List<FarmSeed>>() {}
        );

        List<ProductSeed> productSeeds = readJsonFile(
                "data/products.json",
                new TypeReference<List<ProductSeed>>() {}
        );

        Map<String, FarmCategory> farmCategoriesByName = new HashMap<>();
        Map<String, ProductCategory> productCategoriesByName = new HashMap<>();
        Map<String, Farm> farmsByExternalId = new HashMap<>();

        for (FarmSeed farmSeed : farmSeeds) {
            Farm farm = new Farm();
            farm.setName(farmSeed.name());
            farm.setDescription(farmSeed.description());
            farm.setAddress(farmSeed.address());
            farm.setImage(farmSeed.image());
            farm.setLatitude(farmSeed.latitude());
            farm.setLongitude(farmSeed.longitude());

            for (String categoryName : farmSeed.categories()) {
                FarmCategory category = farmCategoriesByName.computeIfAbsent(categoryName, name -> {
                    FarmCategory newCategory = new FarmCategory();
                    newCategory.setName(name);
                    newCategory.setDescription(name + " farms");
                    return farmCategoryRepository.save(newCategory);
                });

                farm.getCategories().add(category);
            }

            farmRepository.save(farm);
            farmsByExternalId.put(farmSeed.externalId(), farm);
        }

        for (ProductSeed productSeed : productSeeds) {
            Farm farm = farmsByExternalId.get(productSeed.farmExternalId());

            if (farm == null) {
                continue;
            }

            ProductCategory category = productCategoriesByName.computeIfAbsent(productSeed.category(), name -> {
                ProductCategory newCategory = new ProductCategory();
                newCategory.setName(name);
                newCategory.setDescription(name + " products");
                return productCategoryRepository.save(newCategory);
            });

            Product product = new Product();
            product.setName(productSeed.name());
            product.setDescription(productSeed.description());
            product.setImage(productSeed.image());
            product.setQuantity(productSeed.quantity());
            product.setPrice(productSeed.price());
            product.setCategory(category);
            product.setFarm(farm);

            productRepository.save(product);
        }
    }

    private <T> T readJsonFile(String path, TypeReference<T> typeReference) throws Exception {
        InputStream inputStream = new ClassPathResource(path).getInputStream();
        return objectMapper.readValue(inputStream, typeReference);
    }

    private record FarmSeed(
            String externalId,
            String slug,
            String name,
            String description,
            String address,
            String fullAddress,
            String image,
            Double latitude,
            Double longitude,
            List<String> categories
    ) {
    }

    private record ProductSeed(
            String farmExternalId,
            String name,
            String description,
            Double price,
            Integer quantity,
            Double sizeValue,
            String sizeUnit,
            String image,
            String category
    ) {
    }
}