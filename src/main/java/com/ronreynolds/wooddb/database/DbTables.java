package com.ronreynolds.wooddb.database;

import com.ronreynolds.util.commandline.CommandLines;
import com.ronreynolds.util.jackson.ObjectMappers;
import com.ronreynolds.util.string.Latin1;
import com.ronreynolds.wooddb.data.CombinedWoodData;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Stream;

/**
 * schema info about all tables in the wood_db
 */
@Slf4j
public class DbTables {
    public static final TableMetadata<CombinedWoodData> woodYamlImport = new TableMetadata<>(
            "wood_yaml_import",
            List.of(
                    "latinBinomial", "sourceJson", "woodDatabaseUrl", "imageUrl", "commonName", "binomial", "commonNamesJson",
                    "distribution", "treeSize", "colorAndAppearance", "grainAndTexture", "rotResistance", "workability", "odor",
                    "allergiesAndToxicity", "pricingAndAvailability", "sustainability", "commonUses", "comments",
                    "endGrainImage", "endGrain10xImage", "edgeGrainImage",
                    "averageDryWeight", "specificGravity", "jankaHardness",
                    "ruptureModulus", "elasticModulus", "crushingStrength",
                    "shrinkage", "porosity", "arrangement", "vessels", "parenchyma", "rays", "lookalikes", "notes"
            ),
            (batch, model) -> {
                String scientificName  = Latin1.sanitize(model.scientificName());
                String sourceJson      = Latin1.sanitize(ObjectMappers.toCompactJson(model));
                String woodDatabaseUrl = Latin1.sanitize(model.basicData().pageUrl());
                String imageUrl        = Latin1.sanitize(model.basicData().imageUrl());
                String commonName      = Latin1.sanitize(model.basicData().commonName());
                String binomial        = Latin1.sanitize(model.basicData().binomial());

                String commonNamesJson = ObjectMappers.toCompactJson(
                        Stream.of(model.commonNamesArray()).map(Latin1::sanitize).toArray(String[]::new));
                String distribution           = Latin1.sanitize(model.metaData().distribution());
                String treeSize               = Latin1.sanitize(model.metaData().treeSize());
                String colorAndAppearance     = Latin1.sanitize(model.metaData().colorAndAppearance());
                String grainAndTexture        = Latin1.sanitize(model.metaData().grainAndTexture());
                String rotResistance          = Latin1.sanitize(model.metaData().rotResistance());
                String workability            = Latin1.sanitize(model.metaData().workability());
                String odor                   = Latin1.sanitize(model.metaData().odor());
                String allergiesAndToxicity   = Latin1.sanitize(model.metaData().allergiesAndToxicity());
                String pricingAndAvailability = Latin1.sanitize(model.metaData().pricingAndAvailability());
                String sustainability         = Latin1.sanitize(model.metaData().sustainability());
                String commonUses             = Latin1.sanitize(model.metaData().commonUses());
                String comments               = Latin1.sanitize(model.metaData().comments());

                String endGrainImage    = Latin1.sanitize(model.images().endGrainImage());
                String endGrain10xImage = Latin1.sanitize(model.images().endGrain10xImage());
                String edgeGrainImage   = Latin1.sanitize(model.images().edgeGrainImage());

                String averageDryWeight = Latin1.sanitize(model.materialData().averageDryWeight());
                String specificGravity  = Latin1.sanitize(model.materialData().specificGravity());
                String jankaHardness    = Latin1.sanitize(model.materialData().jankaHardness());
                String ruptureModulus   = Latin1.sanitize(model.materialData().ruptureModulus());
                String elasticModulus   = Latin1.sanitize(model.materialData().elasticModulus());
                String crushingStrength = Latin1.sanitize(model.materialData().crushingStrength());
                String shrinkage        = Latin1.sanitize(model.materialData().shrinkage());

                String notes       = Latin1.sanitize(model.macroscopic().notes());
                String lookalikes  = Latin1.sanitize(model.macroscopic().lookalikes());
                String porosity    = Latin1.sanitize(model.macroscopic().porosity());
                String arrangement = Latin1.sanitize(model.macroscopic().arrangement());
                String vessels     = Latin1.sanitize(model.macroscopic().vessels());
                String parenchyma  = Latin1.sanitize(model.macroscopic().parenchyma());
                String rays        = Latin1.sanitize(model.macroscopic().rays());

                if (CommandLines.getOrDefault("logSizes", false)) {
                    log.info("lengths for {}:\nscientificName:{} json:{} woodDatabaseUrl:{} imageUrl:{} commonName:{} " +
                                    "binomial:{} " +
                                    "commonNamesJson:{} distribution:{} treeSize:{} colorAndAppearance:{} grainAndTexture:{} " +
                                    "rotResistance:{} workability:{} odor:{} allergiesAndToxicity:{} pricingAndAvailability:{} " +
                                    "sustainability:{} commonUses:{} comments:{} endGrainImage:{} endGrain10xImage:{} " +
                                    "edgeGrainImage:{} averageDryWeight:{} specificGravity:{} jankaHardness:{} " +
                                    "ruptureModulus:{} " +
                                    "elasticModulus:{} crushingStrength:{} shrinkage:{} porosity:{} arrangement:{} vessels:{} " +
                                    "parenchyma:{} rays:{} lookalikes:{} notes:{}",
                            scientificName, length(scientificName), length(sourceJson), length(woodDatabaseUrl), length(imageUrl),
                            length(commonName), length(binomial), length(commonNamesJson), length(distribution), length(treeSize),
                            length(colorAndAppearance), length(grainAndTexture), length(rotResistance), length(workability),
                            length(odor), length(allergiesAndToxicity), length(pricingAndAvailability), length(sustainability),
                            length(commonUses), length(comments), length(endGrainImage), length(endGrain10xImage),
                            length(edgeGrainImage), length(averageDryWeight), length(specificGravity), length(jankaHardness),
                            length(ruptureModulus), length(elasticModulus), length(crushingStrength), length(shrinkage),
                            length(porosity), length(arrangement), length(vessels), length(parenchyma), length(rays),
                            length(lookalikes), length(notes));
                }

                batch.bind("latinBinomial", scientificName)
                        .bind("sourceJson", sourceJson)
                        .bind("woodDatabaseUrl", woodDatabaseUrl)
                        .bind("imageUrl", imageUrl)
                        .bind("commonName", commonName)
                        .bind("binomial", binomial)
                        .bind("commonNamesJson", commonNamesJson)
                        .bind("distribution", distribution)
                        .bind("treeSize", treeSize)
                        .bind("colorAndAppearance", colorAndAppearance)
                        .bind("grainAndTexture", grainAndTexture)
                        .bind("rotResistance", rotResistance)
                        .bind("workability", workability)
                        .bind("odor", odor)
                        .bind("allergiesAndToxicity", allergiesAndToxicity)
                        .bind("pricingAndAvailability", pricingAndAvailability)
                        .bind("sustainability", sustainability)
                        .bind("commonUses", commonUses)
                        .bind("comments", comments)
                        .bind("endGrainImage", endGrainImage)
                        .bind("endGrain10xImage", endGrain10xImage)
                        .bind("edgeGrainImage", edgeGrainImage)
                        .bind("averageDryWeight", averageDryWeight)
                        .bind("specificGravity", specificGravity)
                        .bind("jankaHardness", jankaHardness)
                        .bind("ruptureModulus", ruptureModulus)
                        .bind("elasticModulus", elasticModulus)
                        .bind("crushingStrength", crushingStrength)
                        .bind("shrinkage", shrinkage)
                        .bind("porosity", porosity)
                        .bind("arrangement", arrangement)
                        .bind("vessels", vessels)
                        .bind("parenchyma", parenchyma)
                        .bind("rays", rays)
                        .bind("lookalikes", lookalikes)
                        .bind("notes", notes)
                        .add();
            }
    );

    private static Integer length(final CharSequence cs) {
        return cs == null ? null : cs.length();
    }
}
