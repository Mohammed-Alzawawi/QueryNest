package com.example.querynest.query.read.api;

import java.util.List;
import java.util.Set;

public record SelectRequest(
        String tableName,
        List<String> projectedColumns,
        Set<String> filterColumns, // columns used in WHERE
        Integer limit
) {}
