package com.example.querynest.validation.strategy;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.validation.ValidationResult;

public interface ValidationStrategy {
    void validate(CreateTableStatement stmt, ValidationResult result);
}
