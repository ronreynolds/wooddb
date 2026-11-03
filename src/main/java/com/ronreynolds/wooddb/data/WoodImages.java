package com.ronreynolds.wooddb.data;

/**
 * URLs or paths to the 2 best images for macroscopic ID; other images TBD
 */
public record WoodImages(
        String endgrainImage,
        String endgrain10xImage,
        String edgeGrainImage) {
    // backwards-compat ctor
    public WoodImages(String endgrain, String endgrain10x) {
        this(endgrain, endgrain10x, null);
    }
}
