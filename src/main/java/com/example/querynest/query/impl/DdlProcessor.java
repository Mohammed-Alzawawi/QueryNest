package com.example.querynest.query.impl;

import com.example.querynest.api.dto.CreateTableQueryResponse;
import com.example.querynest.api.dto.DropTableResponse;
import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.exception.ParseException;
import com.example.querynest.exception.ValidationException;
import com.example.querynest.query.QueryProcessor;
import com.example.querynest.schema.SchemaRegistry;
import com.example.querynest.service.SchemaService;
import com.example.querynest.validation.ValidationResult;
import org.springframework.stereotype.Component;

@Component
public class DdlProcessor extends QueryProcessor {

    private final SchemaService schemaService;
    private final SchemaRegistry schemaRegistry;

    public DdlProcessor(SchemaService schemaService, SchemaRegistry schemaRegistry) {
        this.schemaService = schemaService;
        this.schemaRegistry = schemaRegistry;
    }

    @Override
    protected Object doProcess(String query) {

        String upper = query.toUpperCase();

        if (upper.startsWith("CREATE")) {
            CreateTableStatement ast = schemaService.parseStatement(query);
            ValidationResult vr = schemaService.processCreateStatement(query);

            return new CreateTableQueryResponse(
                    "success",
                    ast,
                    vr.getWarnings()
            );
        } else if (upper.startsWith("DROP TABLE")) {
            String tableName = extractTableNameFromDrop(query);

            if (!schemaRegistry.tableExists(tableName)) {
                throw new ValidationException(
                        "Table does not exist: " + tableName,
                        java.util.List.of("Table not found in registry")
                );
            }

            schemaRegistry.dropTable(tableName);

            return new DropTableResponse(
                    "success",
                    tableName,
                    "Table dropped successfully"
            );
        }

        throw new UnsupportedOperationException("Unsupported DDL statement. Only CREATE TABLE and DROP TABLE are supported.");
    }

    private String extractTableNameFromDrop(String query) {
        String q = query.trim();
        if (q.endsWith(";")) {
            q = q.substring(0, q.length() - 1);
        }

        String[] parts = q.split("\\s+");
        if (parts.length < 3) {
            throw new ParseException("Expected table name after DROP TABLE");
        }
        return parts[2];
    }
}
