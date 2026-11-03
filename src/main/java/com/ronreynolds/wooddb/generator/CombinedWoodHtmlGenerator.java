package com.ronreynolds.wooddb.generator;

import com.ronreynolds.wooddb.data.CombinedWoodData;
import j2html.Config;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static j2html.TagCreator.a;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.each;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.img;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.strong;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.title;
import static j2html.TagCreator.tr;

@Slf4j
public class CombinedWoodHtmlGenerator {
    private static final Pattern WOOD_DB_IMAGE_URL       = Pattern.compile(
            "https://www.wood-database.com/wp-content/uploads/(.*)");
    private static final String  IMAGE_DIR               = "images/";
    private static final String  COMBINED_WOOD_DATA_YAML = "combined-wood-data.yaml";
    private static final String  HTML_OUTPUT_DIR         = "src/main/resources/combined-wood-data/";

    public static void main(String[] args) throws IOException {
        File                   combinedWoodDataFile = new File(COMBINED_WOOD_DATA_YAML);
        List<CombinedWoodData> combinedWoodData     = CombinedWoodData.loadYaml(combinedWoodDataFile);
        log.info("loaded {} combined wood data records from {}", combinedWoodData.size(), COMBINED_WOOD_DATA_YAML);

        Config.closeEmptyTags = true;
        var head = head(
                meta().withCharset("utf-8"),
                title("Combined Wood Data"),
                link().withRel("stylesheet").withHref("css/main.css")
        );
        var table = table(
                thead(
                        tr(
                                th("Scientific Name"),
                                th("Common Names"),
                                th("Edgegrain"),
                                th("Endgrain"),
                                th("Endgrain 10x"),
                                th("Porosity"),
                                th("Arrangement"),
                                th("Vessels"),
                                th("Parenchyma"),
                                th("Rays"),
                                th("link")
                        )
                ),
                tbody(
                        each(combinedWoodData, data -> tr(
                                td(data.scientificName()),
                                td(data.commonNames()),
                                td(a().withTarget("_blank").withHref(imageToLocalPath(data.edgeGrainUrl())).with(
                                        img().withSrc(imageToLocalPath(data.edgeGrainUrl())))),
                                td(a().withTarget("_blank").withHref(imageToLocalPath(data.endGrainUrl())).with(
                                        img().withSrc(imageToLocalPath(data.endGrainUrl())))),
                                td(a().withTarget("_blank").withHref(imageToLocalPath(data.endGrain10xUrl())).with(
                                        img().withSrc(imageToLocalPath(data.endGrain10xUrl())))),
                                td(nullToBlank(data.porosity())),
                                td(nullToBlank(data.arrangement())),
                                td(nullToBlank(data.vessels())),
                                td(nullToBlank(data.parenchyma())),
                                td(nullToBlank(data.rays())),
                                td(a(data.link()).withHref(data.link()).withTarget("_blank"))
                        ))
                )
        );
        var body = body(
                strong("generated " + DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())),
                br(),
                table
        );

        Files.writeString(Path.of(HTML_OUTPUT_DIR, "combined-wood-data.html"), html(head, body).renderFormatted());
    }

    private static String nullToBlank(String s) {
        return s == null ? "" : s;
    }

    private static String imageToLocalPath(String imageUrl) {
        if (StringUtil.isBlank(imageUrl)) {
            return "";
        }
        // https://www.wood-database.com/wp-content/uploads/alstonia-scholaris4.jpg -> <IMAGE_DIR>/alstonia-scholaris4.jpg
        Matcher mat = WOOD_DB_IMAGE_URL.matcher(imageUrl);
        if (mat.matches()) {
            return IMAGE_DIR + mat.group(1);
        }
        return imageUrl;
    }
}
