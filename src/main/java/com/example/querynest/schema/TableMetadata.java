package com.example.querynest.schema;

import java.util.List;

public record TableMetadata(
        String name,
        List<ColumnMetadata> columns
) {}
