package com.tierrasdeasturias.backend.seeders;

import com.tierrasdeasturias.backend.entities.Farm;
import com.tierrasdeasturias.backend.entities.Product;
import com.tierrasdeasturias.backend.repositories.FarmCategoryRepository;
import com.tierrasdeasturias.backend.repositories.FarmRepository;
import com.tierrasdeasturias.backend.repositories.ProductCategoryRepository;
import com.tierrasdeasturias.backend.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        verify(farms, never()).save(any(Farm.class));
    }

    @Test
    void seedsLogosAndBannersSeparatelyAndAllowsMissingBanners() throws Exception {
        when(farms.count()).thenReturn(0L);
        seeder.run();

        ArgumentCaptor<Farm> captor = ArgumentCaptor.forClass(Farm.class);
        verify(farms, times(68)).save(captor.capture());
        List<Farm> savedFarms = captor.getAllValues();
        Farm saregana = savedFarms.stream()
                .filter(farm -> farm.getName().equals("La Saregana"))
                .findFirst().orElseThrow();
        Farm picurriellu = savedFarms.stream()
                .filter(farm -> farm.getName().contains("Picurriellu"))
                .findFirst().orElseThrow();

        assertEquals("https://lasaregana.es/img/logo-1679657612.jpg", saregana.getImage());
        assertEquals("https://lasaregana.es/img/cms/banner_saregana_3.jpg",
                saregana.getBannerImage());
        assertEquals("https://queseriapicurriellu.com/wp-content/uploads/2020/05/PicuurrieluR.png",
                picurriellu.getImage());
        assertEquals("https://queseriapicurriellu.com/wp-content/uploads/2020/05/Centro-de-elaboracio%CC%81nPhoto.jpg",
                picurriellu.getBannerImage());
        Map<String, String> localFarmSlugs = Map.of(
                "Queser\u00eda Rey Silo", "queseria-rey-silo",
                "Queser\u00eda Ca Llechi", "queseria-ca-llechi",
                "La Solana de Allande", "la-solana-de-allande",
                "Llagar Sidra Crespo", "llagar-sidra-crespo-sales",
                "Finca El Ribeiro", "finca-el-ribeiro"
        );
        for (Map.Entry<String, String> entry : localFarmSlugs.entrySet()) {
            Farm farm = savedFarms.stream()
                    .filter(saved -> saved.getName().equals(entry.getKey()))
                    .findFirst().orElseThrow();
            String basePath = "/images/farms/" + entry.getValue();
            String bannerExtension = entry.getValue().equals("la-solana-de-allande") ? "png" : "jpg";
            assertEquals(basePath + "/logo.png", farm.getImage());
            assertEquals(basePath + "/banner." + bannerExtension, farm.getBannerImage());
        }

        Farm casaElCampo = savedFarms.stream()
                .filter(farm -> farm.getName().equals("Miel Casa El Campo"))
                .findFirst().orElseThrow();
        assertNull(casaElCampo.getBannerImage());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "Queser\u00eda Asiegu, /images/farms/queseria-asiegu/logo.png, /images/farms/queseria-asiegu/banner.jpg",
            "Queser\u00eda Vega de Ario, /images/farms/queseria-vega-de-ario/logo.jpg, /images/farms/queseria-vega-de-ario/banner.jpg",
            "Queser\u00eda Los Caserinos, /images/farms/queseria-los-caserinos/logo.png, /images/farms/queseria-los-caserinos/banner.jpg",
            "Queser\u00eda Demu\u00e9s, /images/farms/queseria-demues/logo.jpg, /images/farms/queseria-demues/banner.jpg",
            "Queser\u00eda Ovin, /images/farms/queseria-ovin/logo.png, /images/farms/queseria-ovin/banner.jpg",
            "Olaya Miel, /images/farms/olaya-miel-aller/logo.png, /images/farms/olaya-miel-aller/banner.png",
            "Er\u00eda de Valles, /images/farms/eria-de-valles-oviedo/logo.png, /images/farms/eria-de-valles-oviedo/banner.jpg",
            "ReinAstur, /images/farms/reinastur-grado/logo.png, /images/farms/reinastur-grado/banner.jpg",
            "Miel LAPUELA, /images/farms/miel-lapuela-cangas-del-narcea/logo.png, /images/farms/miel-lapuela-cangas-del-narcea/banner.webp",
            "Llagar Sidra JR, /images/farms/llagar-sidra-jr-cabuenes/logo.png, /images/farms/llagar-sidra-jr-cabuenes/banner.jpg",
            "Finca Casomera, /images/farms/finca-casomera-casomera/logo.webp, /images/farms/finca-casomera-casomera/banner.webp",
            "Ar\u00e1ndanos La Pe\u00f1a, /images/farms/arandanos-la-pena-salas/logo.png, /images/farms/arandanos-la-pena-salas/banner.jpg",
            "Picar\u00e1n, /images/farms/picaran-finca-ecologica-picaroso-grado/logo.svg, /images/farms/picaran-finca-ecologica-picaroso-grado/banner.jpg",
            "Los Ar\u00e1ndanos, /images/farms/los-arandanos-taramundi/logo.jpg, /images/farms/los-arandanos-taramundi/banner.webp",
            "Finca El Cabill\u00f3n, /images/farms/finca-el-cabillon-tapia-de-casariego/logo.png, /images/farms/finca-el-cabillon-tapia-de-casariego/banner.jpg",
            "Oles Gourmet, /images/farms/oles-gourmet-granja-fitories-villaviciosa/logo.png, /images/farms/oles-gourmet-granja-fitories-villaviciosa/banner.jpg",
            "Bodega Monasterio de Corias, /images/farms/bodega-monasterio-de-corias/logo.svg, /images/farms/bodega-monasterio-de-corias/banner.jpg",
            "Siluvio Bodegas y Vi\u00f1edos, /images/farms/siluvio-bodegas-y-vinedos-ibias/logo.png, /images/farms/siluvio-bodegas-y-vinedos-ibias/banner.jpg",
            "Bodega Vi\u00f1a Cuedo, /images/farms/bodega-vina-cuedo-ibias/logo.png, /images/farms/bodega-vina-cuedo-ibias/banner.jpg",
            "Escolinas, /images/farms/escolinas-cangas/logo.png, /images/farms/escolinas-cangas/banner.jpg"
    })
    void seedsLocalImagesForNextTwentyFarms(
            String farmName, String expectedImage, String expectedBannerImage
    ) throws Exception {
        when(farms.count()).thenReturn(0L);
        seeder.run();

        ArgumentCaptor<Farm> captor = ArgumentCaptor.forClass(Farm.class);
        verify(farms, times(68)).save(captor.capture());
        Farm farm = captor.getAllValues().stream()
                .filter(saved -> saved.getName().equals(farmName))
                .findFirst().orElseThrow();

        assertEquals(expectedImage, farm.getImage());
        assertEquals(expectedBannerImage, farm.getBannerImage());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "Queser\u00eda La Peral, /images/farms/queseria-la-peral/logo.png, /images/farms/queseria-la-peral/banner.png",
            "Queser\u00eda Temia, /images/farms/queseria-temia/card.jpg, /images/farms/queseria-temia/banner.jpg",
            "Queser\u00eda Ca Sanchu, /images/farms/queseria-ca-sanchu/card.jpg, /images/farms/queseria-ca-sanchu/banner.jpg",
            "Queser\u00eda El Cabriteru, /images/farms/queseria-el-cabriteru/card.png, /images/farms/queseria-el-cabriteru/banner.jpg",
            "Queser\u00eda Ma\u00edn, /images/farms/queseria-main/logo.png, /images/farms/queseria-main/banner.jpg",
            "Quesos de Taramundi, /images/farms/quesos-de-taramundi/card.jpg, /images/farms/quesos-de-taramundi/banner.jpg",
            "Queser\u00eda La Pe\u00f1ona, /images/farms/queseria-la-penona/card.jpg, /images/farms/queseria-la-penona/banner.jpg",
            "Leche Leche, /images/farms/leche-leche/logo.png, /images/farms/leche-leche/banner.jpg",
            "Queser\u00eda Vega de Tord\u00edn, /images/farms/queseria-vega-de-tordin/card.jpg, /images/farms/queseria-vega-de-tordin/banner.jpg",
            "Quesos Artesanos Reboll\u00edn, /images/farms/quesos-artesanos-rebollin/card.jpg, /images/farms/quesos-artesanos-rebollin/banner.jpg",
            "Queser\u00eda La Collada, /images/farms/queseria-la-collada/card.jpg, /images/farms/queseria-la-collada/banner.jpg",
            "Queser\u00eda La Borbolla, /images/farms/queseria-la-borbolla/card.jpg, /images/farms/queseria-la-borbolla/banner.jpg",
            "Queser\u00eda El Viso, /images/farms/queseria-el-viso/card.jpg, /images/farms/queseria-el-viso/banner.jpg",
            "Queser\u00eda Alejandro Casielles 1913, /images/farms/queseria-alejandro-casielles-1913/logo.png, /images/farms/queseria-alejandro-casielles-1913/banner.jpg",
            "Queser\u00eda Collera, /images/farms/queseria-collera/logo.png, /images/farms/queseria-collera/banner.jpg",
            "Miel La Calduya, /images/farms/miel-la-calduya-pesoz/logo.webp, /images/farms/miel-la-calduya-pesoz/banner.webp",
            "Miel El Caxiellu, /images/farms/miel-el-caxiellu-grado/card.jpg, /images/farms/miel-el-caxiellu-grado/banner.jpg",
            "El T\u00edo \u00c1ngel Miel de Asturias, /images/farms/el-tio-angel-miel-de-asturias/card.jpg, /images/farms/el-tio-angel-miel-de-asturias/banner.jpg",
            "Miel Outurelos, /images/farms/miel-outurelos-ibias/logo.webp, /images/farms/miel-outurelos-ibias/banner.webp",
            "Miel Fuentes del Narcea, /images/farms/miel-fuentes-del-narcea/card.jpg, /images/farms/miel-fuentes-del-narcea/banner.jpg",
            "Miel Casa El Campo, /images/farms/miel-casa-el-campo-valdes/logo.jpg, ",
            "Miel La Boalesa, /images/farms/miel-la-boalesa-boal/card.jpg, /images/farms/miel-la-boalesa-boal/banner.jpg",
            "Sidra Fran, /images/farms/sidra-fran-lugones/logo.gif, /images/farms/sidra-fran-lugones/banner.jpg",
            "Llagar Sidra Men\u00e9ndez, /images/farms/llagar-sidra-menendez-fano/logo.png, /images/farms/llagar-sidra-menendez-fano/banner.jpg",
            "Llagar Bernueces, /images/farms/llagar-bernueces-castiello/card.png, /images/farms/llagar-bernueces-castiello/banner.jpg",
            "Sidra Cortina, /images/farms/sidra-cortina-amandi/logo.png, /images/farms/sidra-cortina-amandi/banner.jpg",
            "Llagar Herminio, /images/farms/llagar-herminio-colloto/logo.png, /images/farms/llagar-herminio-colloto/banner.jpg",
            "Llagar Sidra Pamirandi, /images/farms/llagar-sidra-pamirandi-asiegu/card.jpg, /images/farms/llagar-sidra-pamirandi-asiegu/banner.jpg",
            "Llagar Casta\u00f1\u00f3n, /images/farms/llagar-castanon-quintueles/logo.png, /images/farms/llagar-castanon-quintueles/banner.jpg",
            "Llagar Alonso, /images/farms/llagar-alonso-cianu/card.jpg, /images/farms/llagar-alonso-cianu/banner.jpg",
            "Sidra Viuda de Angel\u00f3n, /images/farms/sidra-viuda-de-angelon-nava/logo.png, /images/farms/sidra-viuda-de-angelon-nava/banner.jpg",
            "Ar\u00e1ndanos y Manzanas de Mu\u00f1\u00f3, /images/farms/arandanos-y-manzanas-de-muno-siero/card.png, /images/farms/arandanos-y-manzanas-de-muno-siero/banner.jpg",
            "Ar\u00e1ndanos El Caleyu, /images/farms/arandanos-el-caleyu-picos-de-europa/logo.png, /images/farms/arandanos-el-caleyu-picos-de-europa/banner.jpg",
            "Berries Nature Park, /images/farms/berries-nature-park-santa-marina-siero/logo.png, /images/farms/berries-nature-park-santa-marina-siero/banner.jpg",
            "Ar\u00e1ndanos El Cierr\u00f3n, /images/farms/arandanos-el-cierron-fuentes-villaviciosa/logo.png, /images/farms/arandanos-el-cierron-fuentes-villaviciosa/banner.jpg",
            "Bodega Se\u00f1or\u00edo de Ibias, /images/farms/bodega-senorio-de-ibias/logo.png, /images/farms/bodega-senorio-de-ibias/banner.jpg",
            "Bodega Mart\u00ednez Parrondo, /images/farms/bodega-martinez-parrondo-cangas/logo.png, /images/farms/bodega-martinez-parrondo-cangas/banner.jpg",
            "Bodega Las Danzas, /images/farms/bodega-las-danzas-cangas/logo.webp, /images/farms/bodega-las-danzas-cangas/banner.jpg",
            "Bodega La Verdea, /images/farms/bodega-la-verdea-puenticiella/logo.png, /images/farms/bodega-la-verdea-puenticiella/banner.jpg",
            "Bodega VidAs, /images/farms/bodega-vidas-cangas/card.jpg, /images/farms/bodega-vidas-cangas/banner.jpg",
            "Casa Manunca, /images/farms/casa-manunca-cangas/card.png, /images/farms/casa-manunca-cangas/banner.png"
    })
    void seedsLocalImagesForRemainingFarms(
            String farmName, String expectedImage, String expectedBannerImage
    ) throws Exception {
        when(farms.count()).thenReturn(0L);
        seeder.run();

        ArgumentCaptor<Farm> captor = ArgumentCaptor.forClass(Farm.class);
        verify(farms, times(68)).save(captor.capture());
        Farm farm = captor.getAllValues().stream()
                .filter(saved -> saved.getName().equals(farmName))
                .findFirst().orElseThrow();

        assertEquals(expectedImage, farm.getImage());
        assertEquals(expectedBannerImage, farm.getBannerImage());
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
