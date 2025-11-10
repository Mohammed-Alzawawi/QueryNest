package com.example.querynest.schema;

public record ColumnMetadata(
        String name,
        String dataType,
        boolean isNullable
) {}