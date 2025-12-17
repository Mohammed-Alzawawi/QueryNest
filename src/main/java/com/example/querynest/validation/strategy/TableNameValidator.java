package com.example.querynest.validation.strategy;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.schema.SchemaRegistry;
import com.example.querynest.validation.ValidationResult;
//import lombok.RequiredArgsConstructor;

public class TableNameValidator implements ValidationStrategy {

    private final SchemaRegistry registry;
    private final int maxLength;

    public TableNameValidator(SchemaRegistry registry, int maxLength) {
        this.registry = registry;
        this.maxLength = maxLength;
    }

    @Override
    public void validate(CreateTableStatement stmt, ValidationResult result) {
        String name = stmt.tableName();

        if (name.length() > maxLength)
            result.addError("Table name exceeds maximum length: " + maxLength);

        if (!name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$"))
            result.addError("Invalid table name: " + name);

        if (registry.tableExists(name))
            result.addError("Table already exists: " + name);
    }
}
