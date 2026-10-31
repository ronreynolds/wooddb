package com.ronreynolds.wooddb.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ronreynolds.util.jackson.ObjectMappers;
import com.ronreynolds.wooddb.data.enums.GrowthRingTransition;
import com.ronreynolds.wooddb.data.enums.ParenchymaType;
import com.ronreynolds.wooddb.data.enums.Quantity;
import com.ronreynolds.wooddb.data.enums.RayType;
import com.ronreynolds.wooddb.data.enums.RingType;
import com.ronreynolds.wooddb.data.enums.VesselArrangement;
import com.ronreynolds.wooddb.data.enums.VesselContents;
import com.ronreynolds.wooddb.data.enums.VesselSize;
import com.ronreynolds.wooddb.data.enums.WoodType;

import java.io.File;
import java.io.IOException;
import java.util.List;

// based on the classification key in [Identification of Central American, Mexican, and Caribbean Woods](https://research.fs.usda.gov/download/treesearch/64547.pdf)
// one concern is this contains hardwood properties that don't apply to softwood (and vice versa); might want to create 2 types
// to save on wasted ref space
public record WoodCharacteristics (
   WoodType type,
   boolean resinCanals,
   GrowthRingTransition growthRingTransition,
   ParenchymaType axialParenchyma,
   RingType growthRings,
   VesselArrangement vesselArrangement,
   VesselContents vesselContents,
   VesselSize vesselSize,
   RayType rays,
   Quantity growthRingsPerCentimeter
){
    private static final TypeReference<List<WoodCharacteristics>> DATA_LIST = new TypeReference<>() {
    };

    public static void storeYaml(File woodDataFile, List<WoodCharacteristics> woodDataList) throws IOException {
        ObjectMappers.yamlMapper().writerFor(DATA_LIST).writeValue(woodDataFile, woodDataList);
    }

    public static List<WoodCharacteristics> loadYaml(File woodDataFile) throws IOException {
        return ObjectMappers.yamlMapper().readerFor(DATA_LIST).readValue(woodDataFile);
    }

}

