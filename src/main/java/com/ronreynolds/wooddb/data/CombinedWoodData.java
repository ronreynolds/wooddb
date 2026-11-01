package com.ronreynolds.wooddb.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ronreynolds.util.jackson.ObjectMappers;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * collecting all the wood-data records into a single record
 */
public record CombinedWoodData(
        String scientificName,
        BasicWoodData basicData,
        WoodMetaData metaData,
        WoodImages images,
        MaterialData materialData,
        WoodAnatomy macroscopic) {
    private static final TypeReference<List<CombinedWoodData>> DATA_LIST = new TypeReference<>() {
    };

    public static void storeYaml(File woodDataFile, List<CombinedWoodData> woodDataList) throws IOException {
        ObjectMappers.yamlMapper().writerFor(DATA_LIST).writeValue(woodDataFile, woodDataList);
    }

    public static List<CombinedWoodData> loadYaml(File woodDataFile) throws IOException {
        return ObjectMappers.yamlMapper().readerFor(DATA_LIST).readValue(woodDataFile);
    }
}
