package com.ronreynolds.wooddb.extractor.wooddatabase;

import com.ronreynolds.wooddb.data.BasicWoodData;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * given the main wood index from https://www.wood-database.com/ extra the wood-specific page URLs
 */
@Slf4j
public class URLExtractor {
    private static final Pattern combinedNamesPattern = Pattern.compile("^(.*?)\\s*\\((.*?)\\)$");

    public static void main(String[] args) throws Exception {

        File input = new File("src/main/resources/source-sites/wood-database/wood-database.html");
        if (!input.exists()) {
            log.error("File not found: {}", input.getAbsolutePath());
            return;
        }
        Document doc = Jsoup.parse(input, "UTF-8");

        // Map: wood name → (page URL, image URL)
        List<BasicWoodData> woodDataList = new ArrayList<>();

        // all woods are contained within a div of class col-md-6 (note, this is fragile but true as of 2026-10-28)
        Elements woodDivs = doc.select("div.col-md-6");
        /* e.g.,
        <div class="col-md-6">
            <p align="center">
                <a href="https://www.wood-database.com/afrormosia/">
                <img loading="lazy" decoding="async" width="250" height="167"
                     src="./Wood Filter _ The Wood Database_files/pericopsis-elata-fs-250x167.jpg"
                     class="attachment-banner size-banner wp-post-image"
                     alt="">
                <br>
                <strong>Afrormosia</strong><br><em>(Pericopsis elata)</em></a>
            </p><br>
        </div>
        note, we need to extract the common and bionomial names from the text within the anchor
        and the URL is to a local file to be moved to a new images directory
         */
        for (Element woodDiv : woodDivs) {
            Element anchor = woodDiv.select("a[href]").first();
            if (anchor == null) {
                log.warn("No anchor found in wood div");    // TODO - include some kind of div ID?
                continue;
            }
            String  anchorText = anchor.text();
            String  woodUrl    = anchor.absUrl("href");
            Element img        = anchor.select("img").first();
            String  woodImage;
            if (img == null) {
                log.warn("No image found for wood: {}", anchorText);
                woodImage = "";
            } else {
                woodImage = img.attr("src");
            }
            // combinedNames starts with a common name followed by a binomial in parentheses
            Matcher mat        = combinedNamesPattern.matcher(anchorText);
            String  commonName = "";
            String  binomial   = "";
            if (mat.matches()) {
                commonName = mat.group(1);
                binomial = mat.group(2);
            } else {
                log.warn("Failed to extract names from anchor: {}", anchorText);
            }
            woodDataList.add(new BasicWoodData(woodUrl, woodImage, commonName, binomial));
        }

        File woodDataFile = new File("basic-wood-data.yaml");
        BasicWoodData.storeYaml(woodDataFile, woodDataList);
        log.info("Done: {}", woodDataFile.getAbsolutePath());
    }
}
