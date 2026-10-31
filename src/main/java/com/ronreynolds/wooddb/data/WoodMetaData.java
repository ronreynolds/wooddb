package com.ronreynolds.wooddb.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ronreynolds.util.jackson.ObjectMappers;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * based on the wood-database list of fields
 */
public record WoodMetaData(
        String[] commonNames,
        String distribution,
        String treeSize,
        String colorAndAppearance,
        String grainAndTexture,
        String rotResistance,
        String workability,
        String odor,
        String allergiesAndToxicity,
        String pricingAndAvailability,
        String sustainability,
        String commonUses,
        String comments
) {
    private static final TypeReference<List<WoodMetaData>> DATA_LIST = new TypeReference<>() {
    };

    public static void storeYaml(File woodDataFile, List<WoodMetaData> woodDataList) throws IOException {
        ObjectMappers.yamlMapper().writerFor(DATA_LIST).writeValue(woodDataFile, woodDataList);
    }

    public static List<WoodMetaData> loadYaml(File woodDataFile) throws IOException {
        return ObjectMappers.yamlMapper().readerFor(DATA_LIST).readValue(woodDataFile);
    }

}
