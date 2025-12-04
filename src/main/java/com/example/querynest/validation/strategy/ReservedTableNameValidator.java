package com.example.querynest.validation.strategy;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.validation.ValidationResult;
import com.example.querynest.validation.strategy.ValidationStrategy;

import java.util.Set;

public class ReservedTableNameValidator implements ValidationStrategy {

    private static final Set<String> RESERVED = Set.of(
            "system",
            "information_schema",
            "information",
            "tables",
            "columns",
            "metadata",
            "schema",
            "engine"
    );

    @Override
    public void validate(CreateTableStatement stmt, ValidationResult result) {

        String tableName = stmt.tableName().toLowerCase();

        if (RESERVED.contains(tableName)) {
            result.addError("Table name '" + tableName + "' is reserved and cannot be used.");
        }
    }
}
