package com.ronreynolds.wooddb.extractor.wooddatabase;

import com.ronreynolds.wooddb.data.BasicWoodData;
import com.ronreynolds.wooddb.data.CombinedWoodData;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Slf4j
class WoodDataExtractorTest {
    private static final String        TEST_URL       = "https://www.wood-database.com/afrormosia/";
    private static final String        TEST_FILE_PATH =
            "src/main/resources/source-sites/wood-database/wood-pages/afrormosia.html";
    private static final String        TEST_HTML      = assertDoesNotThrow(() -> Files.readString(Path.of(TEST_FILE_PATH)));
    private static final BasicWoodData BASIC_DATA     = assertDoesNotThrow(() -> BasicWoodData.loadYaml("""
            pageUrl: "https://www.wood-database.com/afrormosia/"
            imageUrl: "./Wood Filter _ The Wood Database_files/pericopsis-elata-fs-250x167.jpg"
            commonName: "Afrormosia"
            binomial: "Pericopsis elata"
            """
    ));

    @Test
    void loadingBasicData_works() {
        assertThat(BASIC_DATA)
                .isNotNull()
                .hasFieldOrPropertyWithValue("pageUrl", "https://www.wood-database.com/afrormosia/")
                .hasFieldOrPropertyWithValue("imageUrl",
                        "./Wood Filter _ The Wood Database_files/pericopsis-elata-fs-250x167.jpg")
                .hasFieldOrPropertyWithValue("commonName", "Afrormosia")
                .hasFieldOrPropertyWithValue("binomial", "Pericopsis elata");
    }

    @Test
    void whiteOakShrinkage_notBlank() {
        Document doc = assertDoesNotThrow(() -> WoodDataExtractor.createDocument(new File(
                "src/main/resources/source-sites/wood-database/wood-pages/white-oak.html")));
        CombinedWoodData data = WoodDataExtractor.extractWoodData(BASIC_DATA, doc);
        assertThat(data.materialData().shrinkage())
                .isNotBlank()
                .isEqualTo("Radial: 5.6%, Tangential: 10.5% Volumetric: 16.3%, T/R Ratio: 1.9");
    }

    @Test
    void merbauCommonNames_containsExpected() {
        Document doc = assertDoesNotThrow(() -> WoodDataExtractor.createDocument(new File(
                "src/main/resources/source-sites/wood-database/wood-pages/merbau.html")));
        CombinedWoodData data = WoodDataExtractor.extractWoodData(BASIC_DATA, doc);
        assertThat(data.metaData().commonNames())
                .isNotEmpty()
                .contains("Merbau", "Kwila", "Ipil");
    }

    @Test
    void radiataPineCommonNames_containsExpected() {
        Document doc = assertDoesNotThrow(() -> WoodDataExtractor.createDocument(new File(
                "src/main/resources/source-sites/wood-database/wood-pages/radiata-pine.html")));
        CombinedWoodData data = WoodDataExtractor.extractWoodData(BASIC_DATA, doc);
        assertThat(data.metaData().commonNames())
                .isNotEmpty()
                .contains("Radiata Pine", "Monterey Pine", "Insignis Pine");
    }

    @Test
    void extractWoodDataFromFile_works() {
        Document         doc  = assertDoesNotThrow(() -> WoodDataExtractor.createDocument(new File(TEST_FILE_PATH)));
        CombinedWoodData data = WoodDataExtractor.extractWoodData(BASIC_DATA, doc);
        // Test implementation
        assertThat(data).satisfies(cwd -> {
            assertThat(cwd.scientificName()).isEqualTo("Pericopsis elata");
            assertThat(cwd.basicData()).satisfies(bd -> {
                assertThat(bd.pageUrl()).isEqualTo("https://www.wood-database.com/afrormosia/");
                assertThat(bd.imageUrl())
                        .isEqualTo("./Wood Filter _ The Wood Database_files/pericopsis-elata-fs-250x167.jpg");
                assertThat(bd.commonName()).isEqualTo("Afrormosia");
                assertThat(bd.binomial()).isEqualTo("Pericopsis elata");
            });
            assertThat(cwd.metaData()).satisfies(md -> {
                assertThat(md.commonNames()).containsOnly("Afrormosia", "afromosia", "African teak");
                assertThat(md.distribution()).isEqualTo("West Africa");
                assertThat(md.treeSize()).isEqualTo("100-150 ft tall, 3-5 ft trunk diameter");
                assertThat(md.colorAndAppearance()).isEqualTo("Heartwood is yellowish brown, occasionally with " +
                        "a reddish or olive-colored hue, darkening with age. Narrow sapwood is pale yellow and " +
                        "is clearly differentiated from the heartwood.");
                assertThat(md.grainAndTexture()).isEqualTo("Grain is usually straight, though it can be " +
                        "interlocked. With a fine, uniform texture and good natural luster.");
                assertThat(md.rotResistance()).isEqualTo("Rated as very durable, with good insect/borer resistance.");
                assertThat(md.workability()).isEqualTo("In nearly all regards, afrormosia is easy to work with both hand and " +
                        "machine tools, though surfacing boards with interlocking grain may cause tearout. Other downsides " +
                        "include a slight blunting effect on cutting edges, and the development of dark stains if left in " +
                        "contact with iron in damp conditions. Afrormosia turns, glues, stains, and finishes well.");
                assertThat(md.odor()).isEqualTo("Afrormosia has a distinct odor while being worked.");
                assertThat(md.allergiesAndToxicity()).isEqualTo("Although severe reactions are quite uncommon, " +
                        "afrormosia has been reported as a sensitizer. Usually most common reactions include eye, skin, " +
                        "and respiratory irritation. Afrormosia has also been known to cause nervous system " +
                        "effects, asthma-like symptoms, as well as splinters having an increased chance of getting infected. " +
                        "See the articles Wood Allergies and Toxicity and Wood Dust Safety for more information.");
                assertThat(md.pricingAndAvailability()).isEqualTo("Trade of this species is currently regulated" +
                        " and tightly controlled—though still available as lumber in good sizes. Prices are " +
                        "medium to high for an imported hardwood.");
                assertThat(md.sustainability()).isEqualTo("This wood species is in CITES Appendix II, and is on" +
                        " the IUCN Red List. It is listed as endangered due to a population reduction of over " +
                        "50% in the past three generations, caused by a decline in its natural range, and exploitation.");
                assertThat(md.commonUses()).isEqualTo("Boatbuilding, veneer, flooring, and furniture.");
                assertThat(md.comments()).isEqualTo("Along with iroko (Milicia excelsa), afrormosia is sometimes referred to as " +
                        "African teak, though neither are closely related to genuine teak (Tectona grandis). Compared to teak, " +
                        "afrormosia has similar weight and strength, nearly comparable rot resistance, and a " +
                        "similar appearance (though some pieces can be a yellower golden brown and more closely" +
                        " resemble satinwood, especially when freshly sawed). However, though afrormosia is a lower-cost" +
                        " alternative to genuine teak, it is in itself threatened and is CITES protected.");
            });
            assertThat(cwd.images()).satisfies(img -> {
                assertThat(img.edgeGrainImage())
                        .isEqualTo("https://www.wood-database.com/wp-content/uploads/pericopsis-elata-fs-250x167.jpg");
                assertThat(img.endGrainImage())
                        .isEqualTo("https://www.wood-database.com/wp-content/uploads/pericopsis-elata3.jpg");
                assertThat(img.endGrain10xImage())
                        .isEqualTo("https://www.wood-database.com/wp-content/uploads/pericopsis-elata4.jpg");
            });
            assertThat(cwd.materialData()).satisfies(md -> {
                assertThat(md.averageDryWeight()).isEqualTo("45.3 lbs/ft");
                assertThat(md.specificGravity()).isEqualTo("0.57, 0.72");
                assertThat(md.jankaHardness()).isEqualTo("1,570 lb");
                assertThat(md.ruptureModulus()).isEqualTo("14,920 lb/in");
                assertThat(md.elasticModulus()).isEqualTo("1,715,000 lb/in");
                assertThat(md.crushingStrength()).isEqualTo("9,570 lb/in");
                assertThat(md.shrinkage()).isEqualTo("Radial: 3.2%, Tangential: 6.2%, Volumetric: 9.9%, T/R Ratio: 1.9");
            });

            assertThat(cwd.macroscopic()).satisfies(ma -> {
                assertThat(ma.porosity()).isEqualTo("diffuse porous");
                assertThat(ma.arrangement()).isEqualTo("solitary and radial multiples");
                assertThat(ma.vessels()).isEqualTo("medium to large, few to moderately numerous; brown " +
                        "heartwood deposits occasionally present");
                assertThat(ma.parenchyma()).isEqualTo("vasicentric, lozenge, confluent, and unilateral");
                assertThat(ma.rays()).isEqualTo("narrow width (generally not visible without magnification); " +
                        "normal spacing");
            });

            log.info("data = {}", data);
        });
    }

    @Test
    void extractWoodDataFromHtml_works() {
        Document         doc  = assertDoesNotThrow(() -> WoodDataExtractor.createDocument(TEST_HTML));
        CombinedWoodData data = WoodDataExtractor.extractWoodData(BASIC_DATA, doc);
        // Test implementation
        assertThat(data)
                .isNotNull()
        // TODO
        ;
    }

    @Test
    void urlToPath_works() {
        assertThat(WoodDataExtractor.mapUrlToLocalFile(TEST_URL)).isEqualTo(TEST_FILE_PATH);
    }
}