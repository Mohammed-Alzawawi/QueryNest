package com.example.querynest.schema;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SchemaRegistry {

    private final Map<String, TableMetadata> tables = new ConcurrentHashMap<>();

    public void registerTable(TableMetadata table) {
        tables.put(table.name().toLowerCase(), table);
    }

    public boolean tableExists(String tableName) {
        return tables.containsKey(tableName.toLowerCase());
    }

    public boolean columnExists(String tableName, String columnName) {
        TableMetadata table = tables.get(tableName.toLowerCase());
        if (table == null) return false;

        return table.columns().stream()
                .anyMatch(col -> col.name().equalsIgnoreCase(columnName));
    }

    public TableMetadata getTable(String tableName) {
        return tables.get(tableName.toLowerCase());
    }

    public List<TableMetadata> getAllTables() {
        return List.copyOf(tables.values());
    }

    public void clear() {
        tables.clear();
    }

    public void dropTable(String tableName) {
        tables.remove(tableName.toLowerCase());
    }
}
