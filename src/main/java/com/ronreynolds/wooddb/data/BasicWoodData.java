package com.ronreynolds.wooddb.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ronreynolds.util.jackson.ObjectMappers;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * the most basic of wood data (imageUrl currently points to thumbnail image on wood-database.com)
 */
public record BasicWoodData(
        String pageUrl,
        String imageUrl,
        String commonName,
        String binomial) {
    private static final TypeReference<List<BasicWoodData>> DATA_LIST = new TypeReference<>() {
    };

    public static void storeYaml(File woodDataFile, List<BasicWoodData> woodDataList) throws IOException {
        ObjectMappers.yamlMapper().writerFor(DATA_LIST).writeValue(woodDataFile, woodDataList);
    }

    public static List<BasicWoodData> loadYaml(File woodDataFile) throws IOException {
        return ObjectMappers.yamlMapper().readerFor(DATA_LIST).readValue(woodDataFile);
    }
}