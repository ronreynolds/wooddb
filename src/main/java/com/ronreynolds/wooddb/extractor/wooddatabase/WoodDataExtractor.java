package com.ronreynolds.wooddb.extractor.wooddatabase;

import com.ronreynolds.util.jsoup.JSoup;
import com.ronreynolds.wooddb.data.BasicWoodData;
import com.ronreynolds.wooddb.data.CombinedWoodData;
import com.ronreynolds.wooddb.data.MaterialData;
import com.ronreynolds.wooddb.data.WoodAnatomy;
import com.ronreynolds.wooddb.data.WoodImages;
import com.ronreynolds.wooddb.data.WoodMetaData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.ValidationException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;

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
    private static final Pattern TRIM_COMMA                 = Pattern.compile("\\s*,\\s*");
    private static final Pattern WOOD_DB_URL_PATTERN        = Pattern.compile("https://www.wood-database.com/(.*?)/");
    private static final Pattern WOOD_DB_LOCAL_FILE_PATTERN = Pattern.compile("./Wood Filter _ The Wood Database_files/(.*?)");
    private static final Pattern NO_DATA_AVAILABLE          = Pattern.compile("No data available\\.?†?\\*?");

    // the CSS path values used to find all the bits within the HTML
    private enum CSSPaths {
        // MaterialData
        commonNames("p:has(a[href='https://www.wood-database.com/wood-articles/common-name/'])"),
        commonNamesMalformed("a[href='https://www.wood-database.com/wood-articles/common-name/']", nextSiblingTextMethod()),
        // can contain child tags and we want all the text
        scientificName("p:has(a[href='https://www.wood-database.com/wood-articles/scientific-name/'])",
                trimPrefixTextMethod("Scientific Name:")),
        scientificNameWithItalics("p:has(a[href='https://www.wood-database.com/wood-articles/scientific-name/']) > i"),

        distribution("p:has(a[href='https://www.wood-database.com/wood-articles/distribution/'])"),
        treeSize("p:has(a[href='https://www.wood-database.com/wood-articles/tree-size/'])", withNextSiblingTextMethod("p")),
        averageDriedWeight("p:has(a[href='https://www.wood-database.com/wood-articles/average-dried-weight/'])"),
        specificGravity("p:has(a[href='https://www.wood-database.com/wood-articles/specific-gravity/'])"),
        jankaHardness("p:has(a[href='https://www.wood-database.com/wood-articles/janka-hardness/'])"),
        ruptureModulus("p:has(a[href='https://www.wood-database.com/wood-articles/modulus-of-rupture/'])"),
        elasticModulus("p:has(a[href='https://www.wood-database.com/wood-articles/modulus-of-elasticity/'])"),
        crustingStrength("p:has(a[href='https://www.wood-database.com/wood-articles/crushing-strength/'])"),
        shrinkage("p:has(a[href='https://www.wood-database.com/wood-articles/dimensional-shrinkage/'])",
                withNextSiblingTextMethod("p")),

        // WoodMetaData
        colorAppearance("p:has(a[href='https://www.wood-database.com/wood-articles/wood-color-appearance/'])",
                trimPrefixTextMethod("Color/Appearance: ")),
        grainTexture("p:has(a[href='https://www.wood-database.com/wood-articles/wood-grain-texture/'])",
                trimPrefixTextMethod("Grain/Texture: ")),
        rotResistance("p:has(a[href='https://www.wood-database.com/wood-articles/wood-durability/'])",
                trimPrefixTextMethod("Rot Resistance: ")),
        workability("p:has(a[href='https://www.wood-database.com/wood-articles/wood-workability/'])",
                trimPrefixTextMethod("Workability: ")),
        odor("p:has(a[href='https://www.wood-database.com/wood-articles/wood-odor/'])", trimPrefixTextMethod("Odor: ")),
        allergies("p:has(a[href='https://www.wood-database.com/wood-articles/wood-allergies-and-toxicity/'])",
                trimPrefixTextMethod("Allergies/Toxicity: ")),
        price("p:has(a[href='https://www.wood-database.com/wood-articles/wood-pricing-availability/'])",
                trimPrefixTextMethod("Pricing/Availability: ")),
        sustainability("p:has(a[href='https://www.wood-database.com/wood-articles/restricted-and-endangered-wood-species/'])",
                trimPrefixTextMethod("Sustainability: ")),
        commonUses("p:has(strong:contains(Common Uses:))", trimPrefixTextMethod("Common Uses: ")),
        comments("p:has(strong:contains(Comments:))", trimPrefixTextMethod("Comments: ")),
        relatedSpecies("p:has(strong:contains(Related Species:))", trimPrefixTextMethod("Related Species: ")),

        // WoodImages
        endgrainImage("figure:has(figcaption:matches(end\\s?grain( 1x)?\\)$)) > a", e -> e.absUrl("href")),
        endgrainImage10x("figure:has(figcaption:matches(end\\s?grain 10x)) > a", e -> e.absUrl("href")),

        // WoodAnatomy
        porosity("p:has(a[href='https://www.wood-database.com/wood-articles/hardwood-anatomy/#porosity'])"),
        arrangement("p:has(a[href='https://www.wood-database.com/wood-articles/hardwood-anatomy/#arrangement'])"),
        vessels("p:has(a[href='https://www.wood-database.com/wood-articles/hardwood-anatomy/#size'])"),
        parenchyma("p:has(a[href='https://www.wood-database.com/wood-articles/hardwood-anatomy/#paratracheal'])"),
        rays("p:has(a[href='https://www.wood-database.com/wood-articles/hardwood-anatomy/#rays'])"),

        lookalikes("p:has(strong:contains(Lookalikes/Substitutes:))", trimPrefixTextMethod("Lookalikes/Substitutes: ")),
        notes("p:has(strong:contains(Notes:))", trimPrefixTextMethod("Notes: "));

        /**
         * the compiled CSS path evaluator
         */
        final Evaluator                 eval;
        /**
         * the original CSS path string (kept for logging purposes)
         */
        final String                    cssPath;
        /**
         * how to convert the Element found that matches the CSS path into a String; default is to use ownText()
         */
        final Function<Element, String> getTextMethod;

        CSSPaths(String cssPath) throws ValidationException {
            this(cssPath, Element::ownText);
        }

        CSSPaths(String cssPath, Function<Element, String> getTextMethod) throws ValidationException {
            log.debug("Compiling CSS Path: {}", cssPath);
            this.eval = QueryParser.parse(cssPath);
            this.cssPath = cssPath;
            this.getTextMethod = getTextMethod;
        }

        Optional<Element> select(Document doc) {
            Element e = doc.selectFirst(eval);
            if (e == null) {
                log.info("unable to find {}", cssPath);
            }
            return Optional.ofNullable(e);
        }
        String text(Document doc) {
            Optional<Element> element = select(doc);
            if (element.isEmpty()) {
                return null;
            }
            String text = getTextMethod.apply(element.get());
            text = StringUtils.trimToEmpty(text);
            // filter out the "No data available." and the version with the little footnote marker that appears on some pages
            if (NO_DATA_AVAILABLE.matcher(text).matches()) {
                text = null;
            }
            return text;
        }
        String[] textArray(Document doc) {
            Optional<Element> element = select(doc);
            return element.map(e -> TRIM_COMMA.split(getTextMethod.apply(e).trim())).orElse(null);
        }

        /**
         * returns a Function that will trim the specified prefix from the text of the element, if it exists
         */
        private static Function<Element, String> trimPrefixTextMethod(String prefixToTrim) {
            return e -> {
                String text = e.text();
                return text.startsWith(prefixToTrim) ? text.substring(prefixToTrim.length()) : text;
            };
        }

        /**
         * returns a Function that will return the text of the element and its next sibling if it has the specified tag name
         */
        private static Function<Element, String> withNextSiblingTextMethod(String siblingTagName) {
            return e -> {
                String text = e.ownText();
                if (NO_DATA_AVAILABLE.matcher(text).matches()) {
                    // early exit if the main element has "No data available" text; don't bother checking the sibling
                    return text;
                }
                StringBuilder buf     = new StringBuilder(text);
                Element       sibling = e.nextElementSibling();
                if (sibling != null && sibling.tagName().equals(siblingTagName)) {
                    buf.append(" ").append(sibling.ownText());
                } else {
                    for (Element child : e.children()) {
                        if (child.tagName().equals("span") && child.className().equals("scrunchie")) {
                            // because of <span class="scrunchie"> elements in White Oak's shrinkage this is going to get weird
                            buf.append(" ").append(child.ownText());
                        } else if (child.tagName().equals("a")) {
                            // and Hickory's shrinkage contains an <a> to another wood and we want that wood's name
                            buf.append(" ").append(child.ownText());
                        }
                    }
                }
                return buf.toString();
            };
        }

        private static Function<Element, String> nextSiblingTextMethod() {
            return e -> {
                Node nextSibling = e.nextSibling(); // this handles both Text and Element nodes
                if (nextSibling != null) {
                    return JSoup.text(nextSibling); // can be a few different Node types; JSoup.text() handles a few
                }
                return null;
            };
        }
    }

    public static void main(String[] args) throws IOException {
        String                 basicWoodYaml        = "src/main/resources/source-sites/wood-database/basic-wood-data.yaml";
        File                   basicWoodDataFile    = new File(basicWoodYaml);
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
            combinedWoodDataList.add(extractWoodData(woodData, createDocument(woodDataHtmlFile)));
        }


        File woodDataFile = new File("combined-wood-data.yaml");
        CombinedWoodData.storeYaml(woodDataFile, combinedWoodDataList);
        log.info("Done: {}", woodDataFile.getAbsolutePath());
    }

    static Document createDocument(File source) throws IOException {
        return Jsoup.parse(source, "UTF-8");
    }

    static Document createDocument(String html) throws IOException {
        return Jsoup.parse(html);
    }

    static CombinedWoodData extractWoodData(BasicWoodData woodData, Document doc) {
        String scientificName = CSSPaths.scientificNameWithItalics.text(doc);   // look for this version first
        if (StringUtils.isBlank(scientificName)) {
            log.info("unable to find default scientific name; checking for italics version");
            scientificName = CSSPaths.scientificName.text(doc);
            if (StringUtils.isBlank(scientificName)) {
                log.warn("failed to find scientific name in wood-specific page; falling back to BasicWoodData");
                scientificName = woodData.binomial();
            }
        }
        String[] commonNames = CSSPaths.commonNames.textArray(doc);
        if (commonNames == null || commonNames.length == 0) {
            log.info("unable to find common names; checking for malformed version");
            commonNames = CSSPaths.commonNamesMalformed.textArray(doc);
        }
        return new CombinedWoodData(
                scientificName,
                woodData,
                new WoodMetaData(
                        commonNames,
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
                        CSSPaths.endgrainImage.text(doc),
                        CSSPaths.endgrainImage10x.text(doc),
                        mapLocalFileToWoodDBUrl(woodData.imageUrl())),
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
                        CSSPaths.notes.text(doc))
        );
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

    static String mapLocalFileToWoodDBUrl(String localFile) {
        // src/main/resources/source-sites/wood-database/wood-pages/cape-holly.html -> https://www.wood-database.com/cape-holly/
        Matcher mat = WOOD_DB_LOCAL_FILE_PATTERN.matcher(localFile);
        if (mat.matches()) {
            return "https://www.wood-database.com/wp-content/uploads/" + mat.group(1);
        } else {
            log.info("malformed local file path: {}", localFile);
            return localFile;
        }
    }
}
