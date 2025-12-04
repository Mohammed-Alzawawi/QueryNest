package com.example.querynest.validation.impl;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.schema.SchemaRegistry;
import com.example.querynest.validation.SchemaValidator;
import com.example.querynest.validation.ValidationResult;
import com.example.querynest.validation.strategy.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CompositeValidator implements SchemaValidator {

    private final List<ValidationStrategy> strategies;

    public CompositeValidator(
            SchemaRegistry registry,
            @Value("${parser.supported-data-types}") List<String> supportedTypes,
            @Value("${parser.max-table-name-length}") int maxTableLength,
            @Value("${parser.max-column-name-length}") int maxColumnLength
    ) {
        this.strategies = List.of(
                new TableNameValidator(registry, maxTableLength),
                new ColumnValidator(supportedTypes, maxColumnLength),
                new ConstraintValidator(registry)
        );
    }

    @Override
    public ValidationResult validate(CreateTableStatement stmt) {
        ValidationResult result = new ValidationResult();
        strategies.forEach(s -> s.validate(stmt, result));
        return result;
    }
}
