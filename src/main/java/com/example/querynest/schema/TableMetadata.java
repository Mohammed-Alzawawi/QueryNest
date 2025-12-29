package com.example.querynest.schema;

import com.example.querynest.ast.ColumnDefinition;
import com.example.querynest.ast.constraints.Constraint;

import java.util.List;

import static java.util.stream.Collectors.toList;

public record TableMetadata(
        String name,
        List<ColumnDefinition>columns,
        List<Constraint> constraints,
        String engine,
        String uuid
) {

    public boolean hasColumn(String columnName) {
        return columns.stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(columnName));
    }

    public ColumnDefinition getColumn(String columnName) {
        return columns.stream()
                .filter(c -> c.getName().equalsIgnoreCase(columnName))
                .findFirst()
                .orElse(null);
    }

    public List<String> columnNames() {
        return columns.stream()
                .map(ColumnDefinition::getName)
                .toList();
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public boolean isNullable(String columnName) {
        ColumnDefinition col = getColumn(columnName);
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
