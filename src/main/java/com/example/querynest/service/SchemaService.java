package com.example.querynest.service;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.ast.InsertStatement;
import com.example.querynest.validation.ValidationResult;

public interface SchemaService {
    ValidationResult processCreateStatement(String rawStatement);
    CreateTableStatement parseCreateStatement(String rawStatement);
    String generateSql(CreateTableStatement statement);
    InsertStatement parseInsertStatement(String rawStatement);
    ValidationResult processInsertStatement(String rawStatement);
}
