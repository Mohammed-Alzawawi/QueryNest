package com.example.querynest.ast;

import com.example.querynest.ast.filter.FilterCondition;
import java.util.List;

public record SelectStatement(
        String tableName,
        List<String> projectedColumns,
        List<FilterCondition> filterColumns,
        Integer limit    // null = no LIMIT
) implements Statement {}
