package com.ronreynolds.wooddb.generator;

import com.ronreynolds.wooddb.data.CombinedWoodData;
import j2html.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static j2html.TagCreator.*;  // yuck

@Slf4j
public class CombinedWoodHtmlGenerator {
    public static void main(String[] args) throws IOException {
        final String           combinedWoodDataPath = "combined-wood-data.yaml";
        File                   combinedWoodDataFile = new File(combinedWoodDataPath);
        List<CombinedWoodData> combinedWoodData     = CombinedWoodData.loadYaml(combinedWoodDataFile);
        log.info("loaded {} combined wood data records from {}", combinedWoodData.size(), combinedWoodDataPath);

        Config.closeEmptyTags = true;
        var head = head(
                title("Combined Wood Data"),
                link().withRel("stylesheet").withHref("/css/main.css")
        );
        var table = table(
                thead(
                        tr(
                                th("Scientific Name"),
                                th("Common Names"),
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
                                td(img().withSrc(nullToBlank(data.endgrainUrl()))),
                                td(img().withSrc(nullToBlank(data.endgrain10xUrl()))),
                                td(nullToBlank(data.porosity())),
                                td(nullToBlank(data.arrangement())),
                                td(nullToBlank(data.vessels())),
                                td(nullToBlank(data.parenchyma())),
                                td(nullToBlank(data.rays())),
                                td(a(data.link()).withHref(data.link()))
                        ))
                )
        );
        var body = body(
                strong("generated " + DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now())),
                br(),
                table
        );

        Files.writeString(Path.of("combined-wood-data.html"), html(head, body).renderFormatted());
    }

    private static String nullToBlank(String s) {
        return s == null ? "" : s;
    }
}
