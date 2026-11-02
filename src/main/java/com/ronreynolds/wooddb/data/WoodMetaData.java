package com.ronreynolds.wooddb.data;

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
}
