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

    public String commonNames() {
        return String.join(", ", metaData.commonNameList());
    }
    public String endgrainUrl() {
        return images.endgrainImage();
    }
    public String endgrain10xUrl() {
        return images.endgrain10xImage();
    }

    public String porosity() {
        return macroscopic.porosity();
    }
    public String arrangement() {
        return macroscopic.arrangement();
    }
    public String vessels() {
        return macroscopic.vessels();
    }
    public String parenchyma() {
        return macroscopic.parenchyma();
    }
    public String rays() {
        return macroscopic.rays();
    }

    public String link() {
        return basicData.pageUrl();
    }

    // reading and writing lists of this type to/from file
    private static final TypeReference<List<CombinedWoodData>> DATA_LIST = new TypeReference<>() {
    };

    public static void storeYaml(File woodDataFile, List<CombinedWoodData> woodDataList) throws IOException {
        ObjectMappers.yamlMapper().writerFor(DATA_LIST).writeValue(woodDataFile, woodDataList);
    }

    public static List<CombinedWoodData> loadYaml(File woodDataFile) throws IOException {
        return ObjectMappers.yamlMapper().readerFor(DATA_LIST).readValue(woodDataFile);
    }
}
