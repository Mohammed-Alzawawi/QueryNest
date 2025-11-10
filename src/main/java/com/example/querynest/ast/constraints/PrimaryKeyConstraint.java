package com.example.querynest.ast.constraints;

import java.util.List;

public record PrimaryKeyConstraint(
        String name,
        List<String> columnNames
) implements Constraint {}