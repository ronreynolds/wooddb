package com.ronreynolds.wooddb.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ronreynolds.util.jackson.ObjectMappers;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * this list is based on the wood-database's list of identifying anatomical bits.
 * TODO - convert to enums as much as possible
 */
public record WoodAnatomy(
        String porosity,
        String arrangement,
        String vessels,
        String parenchyma,
        String rays,
        String lookalikes,
        String notes
) {
    private static final TypeReference<List<WoodAnatomy>> DATA_LIST = new TypeReference<>() {
    };

    public static void storeYaml(File woodDataFile, List<WoodAnatomy> woodDataList) throws IOException {
        ObjectMappers.yamlMapper().writerFor(DATA_LIST).writeValue(woodDataFile, woodDataList);
    }

    public static List<WoodAnatomy> loadYaml(File woodDataFile) throws IOException {
        return ObjectMappers.yamlMapper().readerFor(DATA_LIST).readValue(woodDataFile);
    }

}
