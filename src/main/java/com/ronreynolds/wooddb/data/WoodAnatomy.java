package com.ronreynolds.wooddb.data;

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
}
