package com.tierrasdeasturias.backend.seeders;

import com.tierrasdeasturias.backend.entities.Farm;
import com.tierrasdeasturias.backend.entities.Product;
import com.tierrasdeasturias.backend.repositories.FarmCategoryRepository;
import com.tierrasdeasturias.backend.repositories.FarmRepository;
import com.tierrasdeasturias.backend.repositories.ProductCategoryRepository;
import com.tierrasdeasturias.backend.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DataSeederTest {
    private final FarmRepository farms = mock(FarmRepository.class);
    private final ProductRepository products = mock(ProductRepository.class);
    private final Product product = new Product();
    private final DataSeeder seeder = new DataSeeder(
            farms, mock(FarmCategoryRepository.class),
            products, mock(ProductCategoryRepository.class));

    @BeforeEach
    void setUp() {
        when(farms.count()).thenReturn(1L);
        Farm farm = new Farm();
        farm.setName("La Saregana");
        product.setName("Yogur natural");
        product.setFarm(farm);
        when(products.findByNameAndFarm_Name("Yogur natural", "La Saregana"))
                .thenReturn(List.of(product));
    }

    @Test
    void doesNotUpdateProductsWhenFlagIsDisabled() throws Exception {
        seeder.run();
        verifyNoInteractions(products);
    }

    @Test
    void fillsMissingSizesFromJson() throws Exception {
        ReflectionTestUtils.setField(seeder, "fillProductSizes", true);
        seeder.run();
        assertEquals(140.0, product.getSizeValue());
        assertEquals("g", product.getSizeUnit());
        verify(products).save(product);
    }

    @Test
    void preservesExistingSizes() throws Exception {
        product.setSizeValue(250.0);
        product.setSizeUnit("ml");
        ReflectionTestUtils.setField(seeder, "fillProductSizes", true);
        seeder.run();
        assertEquals(250.0, product.getSizeValue());
        assertEquals("ml", product.getSizeUnit());
        verify(products, never()).save(any(Product.class));
    }
}
