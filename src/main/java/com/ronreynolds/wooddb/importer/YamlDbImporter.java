package com.ronreynolds.wooddb.importer;

import com.ronreynolds.wooddb.data.CombinedWoodData;
import com.ronreynolds.wooddb.database.DbTables;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * imports the combined-wood-data YAML file into a database table for subsequent processing into a relational schema
 */
@Slf4j
public class YamlDbImporter {
    private static final String COMBINED_WOOD_DATA = "combined-wood-data.yaml";
    private static final int BATCH_SIZE = 1;

    public static void main(String[] args) throws IOException {
        File combinedWoodDataFile = new File(COMBINED_WOOD_DATA);
        List<CombinedWoodData> woodDataList = CombinedWoodData.loadYaml(combinedWoodDataFile);
        log.info("loaded {} wood data records from {}", woodDataList.size(), combinedWoodDataFile.getAbsolutePath());

        Jdbi jdbi = Jdbi.create(System.getenv("WOOD_DB_URL"), System.getenv("WOOD_DB_USER"), System.getenv("WOOD_DB_PASSWORD"));
        log.info("connected to database");

        jdbi.useHandle(handle -> {
            PreparedBatch batch = DbTables.woodYamlImport.prepareBatchInsert(handle);
            int batchSize = 0;
            for (CombinedWoodData item : woodDataList) {
                log.info("adding {} to batch", item.scientificName());
                DbTables.woodYamlImport.bindToBatch(batch, item);
                if (batchSize++ == BATCH_SIZE) {
                    log.info("executing batch insert");
                    try {
                        batch.execute();
                    } catch (Exception sex) {
                        log.error("failed to insert {}", item);
                        log.error("failure: {}", sex, sex);
                        throw sex;  // leave it fatal
                    }
                    batchSize = 0;
                }
            }
            if (batchSize > 0) {
                log.info("executing final batch insert");
                batch.execute();
            }
        });
    }
}
/*
In Phase 2, JDBI automatically maps rows from your staging table into Java records and returns auto-generated primary keys
(executeAndReturnGeneratedKeys()) when populating reference tables.
• Convert YAML to JSON Before Storing.
MariaDB does not have a native YAML column type, but it has built-in JSON functions (JSON_VALUE, JSON_EXTRACT).
Converting each YAML item into a JSON string in Java (using Jackson's YAMLMapper and ObjectMapper) before inserting lets you query
nested fields directly in SQL later if you realize you missed a field during the initial import.

Step 3: Phase 2 Schema Normalization Tips

When you move on to breaking those staging rows into reference tables and enums:
• Wrap Each Record in a Transaction.
Use jdbi.useTransaction(handle -> { ... }) per staging row so that if a lookup or foreign-key insert fails halfway through,
that row rolls back cleanly and stays marked processed = FALSE.
• Use INSERT IGNORE or ON DUPLICATE KEY UPDATE for Lookup Tables.
For reference tables, insert the lookup value and fetch its ID in one clean helper method before inserting the main entity row.

Would you like to see a quick Maven/Gradle dependency snippet for JDBI + Jackson YAML, or an
example of how to cleanly handle reference-table ID lookups inside a JDBI transaction?
 */