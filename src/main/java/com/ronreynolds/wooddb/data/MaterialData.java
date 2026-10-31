package com.ronreynolds.wooddb.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ronreynolds.util.jackson.ObjectMappers;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * based on the wood-database list of fields
 */
public record MaterialData(
        String averageDryWeight,
        String specificGravity,
        String jankaHardness,
        String ruptureModulus,
        String elasticModulus,
        String crushingStrength,
        String shrinkage) {

    private static final TypeReference<List<MaterialData>> DATA_LIST = new TypeReference<>() {
    };

    public static void storeYaml(File woodDataFile, List<MaterialData> woodDataList) throws IOException {
        ObjectMappers.yamlMapper().writerFor(DATA_LIST).writeValue(woodDataFile, woodDataList);
    }

    public static List<MaterialData> loadYaml(File woodDataFile) throws IOException {
        return ObjectMappers.yamlMapper().readerFor(DATA_LIST).readValue(woodDataFile);
    }
}
