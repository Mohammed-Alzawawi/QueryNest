package com.example.querynest.ast;

import com.example.querynest.ast.constraints.Constraint;

import java.util.List;

public record CreateTableStatement(
        String tableName,
        List<ColumnDefinition> columns,
        List<Constraint> constraints,
        List<String> orderBy,
        String engine
) implements Statement {

    public CreateTableStatement {
        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Table must have at least one column");
        }
        if (orderBy == null) {
            throw new IllegalArgumentException("orderBy cannot be null");
        }
    }
}
