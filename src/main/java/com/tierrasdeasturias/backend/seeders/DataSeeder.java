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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final FarmRepository farmRepository;
    private final FarmCategoryRepository farmCategoryRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.data.fill-product-sizes:false}")
    private boolean fillProductSizes;

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
    @Transactional(rollbackFor = Exception.class)
    public void run(String... args) throws Exception {
        if (farmRepository.count() > 0 || productRepository.count() > 0) {
            if (fillProductSizes) {
                int updated = fillMissingProductSizes();
                log.info("Filled sizes for {} products", updated);
            }
            return;
        }

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
            farm.setBannerImage(farmSeed.bannerImage());
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
            product.setSizeValue(productSeed.sizeValue());
            product.setSizeUnit(productSeed.sizeUnit());
            product.setCategory(category);
            product.setFarm(farm);

            productRepository.save(product);
        }
    }

    private int fillMissingProductSizes() throws Exception {
        List<FarmSeed> farmSeeds = readJsonFile(
                "data/farmers.json",
                new TypeReference<List<FarmSeed>>() {}
        );
        List<ProductSeed> productSeeds = readJsonFile(
                "data/products.json",
                new TypeReference<List<ProductSeed>>() {}
        );

        Map<String, String> farmNames = new HashMap<>();
        for (FarmSeed farmSeed : farmSeeds) {
            farmNames.put(farmSeed.externalId(), farmSeed.name());
        }

        int updated = 0;

        for (ProductSeed seed : productSeeds) {
            if (seed.sizeValue() == null || seed.sizeValue() <= 0
                    || seed.sizeUnit() == null || seed.sizeUnit().isBlank()) {
                continue;
            }

            String farmName = farmNames.get(seed.farmExternalId());
            if (farmName == null) continue;

            List<Product> matches = productRepository.findByNameAndFarm_Name(
                    seed.name(), farmName
            );
            if (matches.size() != 1) continue;

            Product product = matches.getFirst();
            if (!seed.name().equals(product.getName())
                    || !farmName.equals(product.getFarm().getName())) continue;
            if (product.getSizeValue() != null
                    || product.getSizeUnit() != null) continue;

            product.setSizeValue(seed.sizeValue());
            product.setSizeUnit(seed.sizeUnit());
            productRepository.save(product);
            updated++;
        }

        return updated;
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
            String bannerImage,
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
