package com.ronreynolds.wooddb.extractor.wooddatabase;

import com.ronreynolds.wooddb.data.BasicWoodData;
import com.ronreynolds.wooddb.data.CombinedWoodData;
import com.ronreynolds.wooddb.data.MaterialData;
import com.ronreynolds.wooddb.data.WoodAnatomy;
import com.ronreynolds.wooddb.data.WoodImages;
import com.ronreynolds.wooddb.data.WoodMetaData;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.helper.ValidationException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * extracts wood data from each wood-specific HTML page and writes it all as YAML back to disc
 */
@Slf4j
public class WoodDataExtractor {
    private static final Pattern TRIM_COMMA          = Pattern.compile("\\s*,\\s*");
    private static final Pattern WOOD_DB_URL_PATTERN = Pattern.compile("https://www.wood-database.com/(.*?)/");

    // the CSS path values used to find all the bits within the HTML
    private enum CSSPaths {
        // MaterialData
        commonNames("p:has(a[href='https://www.wood-database.com/wood-articles/common-name/'])"),
        scientificName("p:has(a[href='https://www.wood-database.com/wood-articles/scientific-name/'])"),
        scientificNameWithItalics("p:has(a[href='https://www.wood-database.com/wood-articles/scientific-name/']) > i"),

        distribution("p:has(a[href='https://www.wood-database.com/wood-articles/distribution/'])"),
        treeSize("p:has(a[href='https://www.wood-database.com/wood-articles/tree-size/'])"),
        averageDriedWeight("p:has(a[href='https://www.wood-database.com/wood-articles/average-dried-weight/'])"),
        specificGravity("p:has(a[href='https://www.wood-database.com/wood-articles/specific-gravity/'])"),
        jankaHardness("p:has(a[href='https://www.wood-database.com/wood-articles/janka-hardness/'])"),
        ruptureModulus("p:has(a[href='https://www.wood-database.com/wood-articles/modulus-of-rupture/'])"),
        elasticModulus("p:has(a[href='https://www.wood-database.com/wood-articles/modulus-of-elasticity/'])"),
        crustingStrength("p:has(a[href='https://www.wood-database.com/wood-articles/crushing-strength/'])"),
        shrinkage("p:has(a[href='https://www.wood-database.com/wood-articles/dimensional-shrinkage/'])"),

        // WoodMetaData
        colorAppearance("p:has(a[href='https://www.wood-database.com/wood-articles/wood-color-appearance/'])"),
        grainTexture("p:has(a[href='https://www.wood-database.com/wood-articles/wood-grain-texture/'])"),
        rotResistance("p:has(a[href='https://www.wood-database.com/wood-articles/wood-durability/'])"),
        workability("p:has(a[href='https://www.wood-database.com/wood-articles/wood-workability/'])"),
        odor("p:has(a[href='https://www.wood-database.com/wood-articles/wood-odor/'])"),
        allergies("p:has(a[href='https://www.wood-database.com/wood-articles/wood-allergies-and-toxicity/'])"),
        price("p:has(a[href='https://www.wood-database.com/wood-articles/wood-pricing-availability/'])"),
        sustainability("p:has(a[href='https://www.wood-database.com/wood-articles/restricted-and-endangered-wood-species/'])"),
        commonUses("p:has(strong:contains(Common Uses:))"),
        comments("p:has(strong:contains(Comments:))"),
        relatedSpecies("p:has(strong:contains(Related Species:))"),

        // WoodImages
        endgrainImage("a[href$='endgrain.jpg']", e -> e.absUrl("href")),
        endgrainImage10x("a[href$='endgrain-zoom.jpg']", e -> e.absUrl("href")),
        endgrainImage2("figure:has(figcaption:contains(endgrain 1x)) > a", e -> e.absUrl("href")),
        endgrainImage10x2("figure:has(figcaption:contains(endgrain 10x)) > a", e -> e.absUrl("href")),

        // WoodAnatomy
        porosity("a[href='https://www.wood-database.com/wood-articles/hardwood-anatomy/#porosity']"),
        arrangement("a[href='https://www.wood-database.com/wood-articles/hardwood-anatomy/#arrangement']"),
        vessels("a[href='https://www.wood-database.com/wood-articles/hardwood-anatomy/#size']"),
        parenchyma("a[href='https://www.wood-database.com/wood-articles/hardwood-anatomy/#paratracheal']"),
        rays("a[href='https://www.wood-database.com/wood-articles/hardwood-anatomy/#rays']"),

        lookalikes("p:has(strong:contains(Lookalikes/Substitutes:))"),
        notes("p:has(strong:contains(Notes:))");

        final String cssPath;
        final Function<Element, String> getTextMethod;

        CSSPaths(String cssPath) throws ValidationException {
            this(cssPath, Element::ownText);
        }

        CSSPaths(String cssPath, Function<Element, String> getTextMethod) throws ValidationException {
            log.info("Compiling CSS Path: {}", cssPath);
            this.cssPath = cssPath;
            this.getTextMethod = getTextMethod;
        }

        Optional<Element> select(Document doc) {
            return Optional.ofNullable(doc.selectFirst(cssPath));
        }
        String text(Document doc) {
            return select(doc).map(getTextMethod).orElseGet(() -> { log.info("unable to find {}", cssPath); return null; });
        }
        String[] textArray(Document doc) {
            return select(doc).map(element -> TRIM_COMMA.split(getTextMethod.apply(element).trim())).orElse(null);
        }
    }

    public static void main(String[] args) throws IOException {
        String basicWoodYaml = "src/main/resources/source-sites/wood-database/basic-wood-data.yaml";
        File basicWoodDataFile = new File(basicWoodYaml);
        List<BasicWoodData>    basicWoodData        = BasicWoodData.loadYaml(basicWoodDataFile);
        List<CombinedWoodData> combinedWoodDataList = new ArrayList<>();

        log.info("loaded info on {} basic woods from {}", basicWoodData.size(), basicWoodDataFile.getAbsolutePath());

        for (BasicWoodData woodData : basicWoodData) {
            log.info("processing {}", woodData.pageUrl());
            File woodDataHtmlFile = new File(mapUrlToLocalFile(woodData.pageUrl()));
            if (!woodDataHtmlFile.exists()) {
                log.warn("{} doesn't exist; skipping", woodDataHtmlFile.getAbsolutePath());
                continue;
            }

            Document doc = Jsoup.parse(woodDataHtmlFile, "UTF-8");
            String scientificName = CSSPaths.scientificName.text(doc);
            if (scientificName == null || scientificName.isBlank()) {
                log.info("unable to find default scientific name; checking for italics version");
                scientificName = CSSPaths.scientificNameWithItalics.text(doc);
            }
            combinedWoodDataList.add(
                    new CombinedWoodData(
                            scientificName,
                            woodData,
                            new WoodMetaData(
                                    CSSPaths.commonNames.textArray(doc),
                                    CSSPaths.distribution.text(doc),
                                    CSSPaths.treeSize.text(doc),
                                    CSSPaths.colorAppearance.text(doc),
                                    CSSPaths.grainTexture.text(doc),
                                    CSSPaths.rotResistance.text(doc),
                                    CSSPaths.workability.text(doc),
                                    CSSPaths.odor.text(doc),
                                    CSSPaths.allergies.text(doc),
                                    CSSPaths.price.text(doc),
                                    CSSPaths.sustainability.text(doc),
                                    CSSPaths.commonUses.text(doc),
                                    CSSPaths.comments.text(doc)),
                            new WoodImages(
                                    CSSPaths.endgrainImage2.text(doc),
                                    CSSPaths.endgrainImage10x2.text(doc)),
                            new MaterialData(
                                    CSSPaths.averageDriedWeight.text(doc),
                                    CSSPaths.specificGravity.text(doc),
                                    CSSPaths.jankaHardness.text(doc),
                                    CSSPaths.ruptureModulus.text(doc),
                                    CSSPaths.elasticModulus.text(doc),
                                    CSSPaths.crustingStrength.text(doc),
                                    CSSPaths.shrinkage.text(doc)),
                            new WoodAnatomy(
                                    CSSPaths.porosity.text(doc),
                                    CSSPaths.arrangement.text(doc),
                                    CSSPaths.vessels.text(doc),
                                    CSSPaths.parenchyma.text(doc),
                                    CSSPaths.rays.text(doc),
                                    CSSPaths.lookalikes.text(doc),
                                    CSSPaths.notes.text(doc)))
            );
        }


        File woodDataFile = new File("combined-wood-data.yaml");
        CombinedWoodData.storeYaml(woodDataFile, combinedWoodDataList);
        log.info("Done: {}", woodDataFile.getAbsolutePath());
    }

    static String mapUrlToLocalFile(String url) {
        // pageUrl: "https://www.wood-database.com/cape-holly/"
        // src/main/resources/source-sites/wood-database/wood-pages/cape-holly.html
        Matcher mat = WOOD_DB_URL_PATTERN.matcher(url);
        if (mat.matches()) {
            return "src/main/resources/source-sites/wood-database/wood-pages/" + mat.group(1) + ".html";
        } else {
            throw new IllegalArgumentException("malformed url " + url);
        }
    }
}
