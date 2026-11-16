package com.ronreynolds.wooddb.database;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public record TableMetadata<Model>(String tableName, List<String> columnNames, BiConsumer<PreparedBatch, Model> bindFunction) {
    public PreparedBatch prepareBatchInsert(Handle handle) {
        return handle.prepareBatch(
                "insert into " + tableName + " (" + String.join(",", columnNames) + ")" +
                        "values (" + columnNames.stream().map(name -> ":" + name).collect(Collectors.joining(",")) + ")"
        );
    }
    public void bindToBatch(PreparedBatch batch, Model type) {
        bindFunction.accept(batch, type);
    }
}
