package com.ronreynolds.wooddb.database;

import com.ronreynolds.util.jackson.ObjectMappers;
import com.ronreynolds.wooddb.data.CombinedWoodData;

import java.util.List;

/**
 * schema info about all tables in the wood_db
 */
public class DbTables {
    public static final TableMetadata<CombinedWoodData> woodYamlImport = new TableMetadata<>(
            "wood_yaml_import",
            List.of(
                    "latinBinomial", "sourceJson", "pageUrl", "imageUrl", "commonName", "binomial", "commonNamesTsv",
                    "distribution", "treeSize", "colorAndAppearance", "grainAndTexture", "rotResistance", "workability", "odor",
                    "allergiesAndToxicity", "pricingAndAvailability", "sustainability", "commonUses", "comments",
                    "endGrainImage", "endGrain10xImage", "edgeGrainImage",
                    "averageDryWeight", "specificGravity", "jankaHardness",
                    "ruptureModulus", "elasticModulus", "crushingStrength",
                    "shrinkage", "porosity", "arrangement", "vessels", "parenchyma", "rays", "lookalikes", "notes"
            ),
            (batch, model) -> {
                // since MariaDB doesn't support YAML as a native type, we'll store it as JSON
                String modelAsJson    = ObjectMappers.toCompactJson(model);
                String commonNamesTsv = model.metaData().commonNames() == null ? null :
                        String.join(",", model.metaData().commonNames());

                batch
                        .bind("latinBinomial", model.scientificName())
                        .bind("sourceJson", modelAsJson)
                        .bind("pageUrl", model.basicData().pageUrl())
                        .bind("imageUrl", model.basicData().imageUrl())
                        .bind("commonName", model.basicData().commonName())
                        .bind("binomial", model.basicData().binomial())
                        .bind("commonNamesTsv", commonNamesTsv)
                        .bind("distribution", model.metaData().distribution())
                        .bind("treeSize", model.metaData().treeSize())
                        .bind("colorAndAppearance", model.metaData().colorAndAppearance())
                        .bind("grainAndTexture", model.metaData().grainAndTexture())
                        .bind("rotResistance", model.metaData().rotResistance())
                        .bind("workability", model.metaData().workability())
                        .bind("odor", model.metaData().odor())
                        .bind("allergiesAndToxicity", model.metaData().allergiesAndToxicity())
                        .bind("pricingAndAvailability", model.metaData().pricingAndAvailability())
                        .bind("sustainability", model.metaData().sustainability())
                        .bind("commonUses", model.metaData().commonUses())
                        .bind("comments", model.metaData().comments())
                        .bind("endGrainImage", model.images().endGrainImage())
                        .bind("endGrain10xImage", model.images().endGrain10xImage())
                        .bind("edgeGrainImage", model.images().edgeGrainImage())
                        .bind("averageDryWeight", model.materialData().averageDryWeight())
                        .bind("specificGravity", model.materialData().specificGravity())
                        .bind("jankaHardness", model.materialData().jankaHardness())
                        .bind("ruptureModulus", model.materialData().ruptureModulus())
                        .bind("elasticModulus", model.materialData().elasticModulus())
                        .bind("crushingStrength", model.materialData().crushingStrength())
                        .bind("shrinkage", model.materialData().shrinkage())
                        .bind("porosity", model.macroscopic().porosity())
                        .bind("arrangement", model.macroscopic().arrangement())
                        .bind("vessels", model.macroscopic().vessels())
                        .bind("parenchyma", model.macroscopic().parenchyma())
                        .bind("rays", model.macroscopic().rays())
                        .bind("lookalikes", model.macroscopic().lookalikes())
                        .bind("notes", model.macroscopic().notes())
                        .add();
            }
    );
}
