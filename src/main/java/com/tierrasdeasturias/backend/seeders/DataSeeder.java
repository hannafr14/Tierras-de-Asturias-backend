package com.tierrasdeasturias.backend.seeders;

import com.tierrasdeasturias.backend.entities.Farm;
import com.tierrasdeasturias.backend.entities.FarmCategory;
import com.tierrasdeasturias.backend.repositories.FarmCategoryRepository;
import com.tierrasdeasturias.backend.repositories.FarmRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final FarmRepository farmRepository;
    private final FarmCategoryRepository farmCategoryRepository;

    public DataSeeder(
            FarmRepository farmRepository,
            FarmCategoryRepository farmCategoryRepository
    ) {
        this.farmRepository = farmRepository;
        this.farmCategoryRepository = farmCategoryRepository;
    }

    @Override
    public void run(String... args) {
        if (farmRepository.count() > 0) {
            return;
        }

        FarmCategory cheeseCategory = new FarmCategory();
        cheeseCategory.setName("Quesos");
        cheeseCategory.setDescription("Farms specializing in artisan cheese.");

        farmCategoryRepository.save(cheeseCategory);

        Farm farm = new Farm();
        farm.setName("Quesería del Valle");
        farm.setDescription("Tradición quesera desde 1920 en el corazón de los Picos de Europa.");
        farm.setAddress("Cabrales, Asturias");
        farm.setImage("https://images.unsplash.com/photo-1500382017468-9049fed747ef");
        farm.setLatitude(43.3502);
        farm.setLongitude(-4.8442);
        farm.getCategories().add(cheeseCategory);

        farmRepository.save(farm);
    }
}