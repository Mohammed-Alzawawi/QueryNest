package com.example.querynest.schema;

import java.util.List;

public record TableMetadata(
        String name,
        List<ColumnMetadata>columns,
        String engine,
        String uuid
) {

    public boolean hasColumn(String columnName) {
        return columns.stream()
                .anyMatch(c -> c.name().equalsIgnoreCase(columnName));
    }

    public ColumnMetadata getColumn(String columnName) {
        return columns.stream()
                .filter(c -> c.name().equalsIgnoreCase(columnName))
                .findFirst()
                .orElse(null);
    }

    public List<String> columnNames() {
        return columns.stream()
                .map(ColumnMetadata::name)
                .toList();
    }

    public boolean isNullable(String columnName) {
        ColumnMetadata col = getColumn(columnName);
        return col != null && col.isNullable();
    }

    public String engineNormalized() {
        return engine.toLowerCase();
    }

    public String summary() {
        return "%s (engine=%s, columns=%d, uuid=%s)"
                .formatted(name, engine, columns.size(), uuid);
    }
}
