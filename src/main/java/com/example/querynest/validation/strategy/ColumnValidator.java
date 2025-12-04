package com.example.querynest.validation.strategy;

import com.example.querynest.ast.ColumnDefinition;
import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.validation.ValidationResult;

import java.util.HashSet;
import java.util.List;

public class ColumnValidator implements ValidationStrategy {

    private final List<String> supportedTypes;
    private final int maxLength;

    public ColumnValidator(List<String> supportedTypes, int maxLength) {
        this.supportedTypes = supportedTypes;
        this.maxLength = maxLength;
    }

    @Override
    public void validate(CreateTableStatement stmt, ValidationResult result) {
        var unique = new HashSet<String>();

        for (ColumnDefinition col : stmt.columns()) {

            if (!unique.add(col.name().toLowerCase()))
                result.addError("Duplicate column name: " + col.name());

            if (!supportedTypes.contains(col.dataType().toUpperCase()))
                result.addError("Unsupported type: " + col.dataType());

            if (col.name().length() > maxLength)
                result.addError("Column name too long: " + col.name());
        }
    }
}
