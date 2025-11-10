package com.example.querynest.validation;

import com.example.querynest.ast.CreateTableStatement;

public interface SchemaValidator {
    ValidationResult validate(CreateTableStatement statement);
}