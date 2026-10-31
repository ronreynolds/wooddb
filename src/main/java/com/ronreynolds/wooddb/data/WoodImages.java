package com.ronreynolds.wooddb.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ronreynolds.util.jackson.ObjectMappers;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * URLs or paths to the 2 best images for macroscopic ID; other images TBD
 */
public record WoodImages(
        String endgrainImage,
        String endgrain10xImage) {
    private static final TypeReference<List<WoodImages>> DATA_LIST = new TypeReference<>() {
    };

    public static void storeYaml(File woodDataFile, List<WoodImages> woodDataList) throws IOException {
        ObjectMappers.yamlMapper().writerFor(DATA_LIST).writeValue(woodDataFile, woodDataList);
    }

    public static List<WoodImages> loadYaml(File woodDataFile) throws IOException {
        return ObjectMappers.yamlMapper().readerFor(DATA_LIST).readValue(woodDataFile);
    }
}
