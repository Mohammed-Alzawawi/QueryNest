package com.example.querynest.ast;

public record ColumnDefinition(
        String name,
        String dataType,
        boolean isNullable,
        String defaultValue
) {
    public ColumnDefinition {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Column name cannot be null or empty");
        }
        if (dataType == null || dataType.isBlank()) {
            throw new IllegalArgumentException("Data type cannot be null or empty");
        }
    }

    public ColumnDefinition(String name, String dataType, boolean isNullable) {
        this(name, dataType, isNullable, null);
    }
}