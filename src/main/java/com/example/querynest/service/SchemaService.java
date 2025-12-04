package com.example.querynest.service;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.validation.ValidationResult;

public interface SchemaService {
    ValidationResult processCreateStatement(String rawStatement);
    CreateTableStatement parseStatement(String rawStatement);
    String generateSql(CreateTableStatement statement);
}