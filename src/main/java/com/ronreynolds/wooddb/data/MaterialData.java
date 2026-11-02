package com.ronreynolds.wooddb.data;

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
}
