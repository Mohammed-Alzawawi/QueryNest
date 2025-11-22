package com.example.querynest.validation.strategy;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.validation.ValidationResult;
import com.example.querynest.validation.strategy.ValidationStrategy;

import java.util.HashSet;
import java.util.Set;

public class OrderByValidator implements ValidationStrategy {

    @Override
    public void validate(CreateTableStatement stmt, ValidationResult result) {

        if (stmt.orderBy() == null || stmt.orderBy().isEmpty()) {
            return;
        }

        Set<String> columnNames = new HashSet<>(
                stmt.columns().stream()
                        .map(c -> c.name().toLowerCase())
                        .toList()
        );

        Set<String> seen = new HashSet<>();

        for (String orderCol : stmt.orderBy()) {
            String col = orderCol.toLowerCase();

            if (!columnNames.contains(col)) {
                result.addError("ORDER BY column does not exist: " + orderCol);
            }

            if (!seen.add(col)) {
                result.addError("Duplicate column in ORDER BY: " + orderCol);
            }
        }
    }
}
