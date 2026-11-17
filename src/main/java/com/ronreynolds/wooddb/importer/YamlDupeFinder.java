package com.ronreynolds.wooddb.importer;

import com.ronreynolds.wooddb.data.CombinedWoodData;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class YamlDupeFinder {
    public static void main(String[] args) throws IOException {
        File           combinedWoodDataFile = new File("combined-wood-data.yaml");
        List<CombinedWoodData> woodDataList         = CombinedWoodData.loadYaml(combinedWoodDataFile);
        log.info("loaded {} wood data records from {}", woodDataList.size(), combinedWoodDataFile.getAbsolutePath());
        Set<String> scientificNames = new HashSet<>();
        for (CombinedWoodData item : woodDataList) {
            String scientificName = item.scientificName();
            if (!scientificNames.add(scientificName)) {
                log.warn("duplicate scientific name found: {}", scientificName);
            }
        }
    }
}
