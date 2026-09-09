package com.tierrasdeasturias.backend.seeders;

import com.tierrasdeasturias.backend.entities.Farm;
import com.tierrasdeasturias.backend.entities.FarmCategory;
import com.tierrasdeasturias.backend.repositories.FarmCategoryRepository;
import com.tierrasdeasturias.backend.repositories.FarmRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.tierrasdeasturias.backend.entities.Product;
import com.tierrasdeasturias.backend.entities.ProductCategory;
import com.tierrasdeasturias.backend.repositories.ProductCategoryRepository;
import com.tierrasdeasturias.backend.repositories.ProductRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final FarmRepository farmRepository;
    private final FarmCategoryRepository farmCategoryRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;

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
    public void run(String... args) {
        Farm farm;

        if (farmRepository.count() == 0) {
            FarmCategory cheeseCategory = new FarmCategory();
            cheeseCategory.setName("Quesos");
            cheeseCategory.setDescription("Farms specializing in artisan cheese.");

            farmCategoryRepository.save(cheeseCategory);

            farm = new Farm();
            farm.setName("Quesería del Valle");
            farm.setDescription("Tradición quesera desde 1920 en el corazón de los Picos de Europa.");
            farm.setAddress("Cabrales, Asturias");
            farm.setImage("https://images.unsplash.com/photo-1500382017468-9049fed747ef");
            farm.setLatitude(43.3502);
            farm.setLongitude(-4.8442);
            farm.getCategories().add(cheeseCategory);

            farmRepository.save(farm);
        } else {
            farm = farmRepository.findAll().get(0);
        }

        if (productRepository.count() > 0) {
            return;
        }

        ProductCategory productCategory = new ProductCategory();
        productCategory.setName("Quesos");
        productCategory.setDescription("Artisan cheeses from Asturias.");

        productCategoryRepository.save(productCategory);

        Product product = new Product();
        product.setName("Queso Cabrales");
        product.setDescription("Traditional blue cheese from Cabrales.");
        product.setImage("https://images.unsplash.com/photo-1452195100486-9cc805987862");
        product.setQuantity(20);
        product.setPrice(14.50);
        product.setCategory(productCategory);
        product.setFarm(farm);

        productRepository.save(product);
    }
}