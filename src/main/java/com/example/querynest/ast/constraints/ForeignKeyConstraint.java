package com.example.querynest.ast.constraints;

import java.util.List;

public record ForeignKeyConstraint(
        String name,
        List<String> columnNames,
        String referencedTable,
        List<String> referencedColumns
) implements Constraint {}