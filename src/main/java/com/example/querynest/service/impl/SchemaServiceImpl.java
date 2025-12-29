package com.example.querynest.service.impl;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.ast.InsertStatement;
import com.example.querynest.builder.SqlGenerator;
import com.example.querynest.parser.lexer.Lexer;
import com.example.querynest.parser.lexer.Token;
import com.example.querynest.parser.lexer.TokenNavigator;
import com.example.querynest.parser.statement.CreateTableBodyParser;
import com.example.querynest.parser.statement.InsertStatementParser;
import com.example.querynest.schema.TableMetadata;
import com.example.querynest.service.SchemaService;
import com.example.querynest.storage.StorageConfig;
import com.example.querynest.storage.StorageService;
import com.example.querynest.storage.writer.PartWriter;
import com.example.querynest.validation.SchemaValidator;
import com.example.querynest.validation.ValidationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class SchemaServiceImpl implements SchemaService {

    private final Lexer lexer;
    private final CreateTableBodyParser createTableBodyParser;
    private final InsertStatementParser insertStatementParser;
    private final SchemaValidator schemaValidator;
    private final SqlGenerator sqlGenerator;
    private final StorageService storageService;

    @Value("${database.storage.path}")
    private String databasePath;

    public SchemaServiceImpl(Lexer lexer,
                             CreateTableBodyParser createTableBodyParser,
                             InsertStatementParser insertStatementParser,
                             SchemaValidator schemaValidator,
                             SqlGenerator sqlGenerator,
                             StorageService storageService) {
        this.lexer = lexer;
        this.createTableBodyParser = createTableBodyParser;
        this.insertStatementParser = insertStatementParser;
        this.schemaValidator = schemaValidator;
        this.sqlGenerator = sqlGenerator;
        this.storageService = storageService;
    }

    @Override
    public ValidationResult processCreateStatement(String rawStatement) {
        CreateTableStatement stmt = parseCreateStatement(rawStatement);
        ValidationResult result = schemaValidator.validate(stmt);
        if (result.isValid()) {
            storageService.createTable(stmt);
        }
        return result;
    }

    @Override
    public CreateTableStatement parseCreateStatement(String rawStatement) {
        List<Token> tokens = lexer.tokenize(rawStatement);
        TokenNavigator navigator = new TokenNavigator(tokens);
        return createTableBodyParser.parse(navigator);
    }

    public InsertStatement parseInsertStatement(String rawStatement) {
        List<Token> tokens = lexer.tokenize(rawStatement);
        TokenNavigator navigator = new TokenNavigator(tokens);
        return insertStatementParser.parse(navigator);
    }

    @Override
    public String generateSql(CreateTableStatement statement) {
        return sqlGenerator.generate(statement);
    }

    @Override
    public ValidationResult processInsertStatement(String rawStatement) {

        ValidationResult result = new ValidationResult();

        if (rawStatement == null || rawStatement.isBlank()) {
            result.addError("Insert statement is empty");
            return result;
        }

        InsertStatement insertStmt = parseInsertStatement(rawStatement);

        TableMetadata tableSchema;
        try {
            tableSchema = storageService.describeTable(insertStmt.getTableName());
        } catch (Exception e) {
            result.addError("Table does not exist: " + insertStmt.getTableName());
            return result;
        }

        executeInsertWithStorage(insertStmt, tableSchema);
        return result;
    }



    private void executeInsertWithStorage(InsertStatement insertStmt,
                                          TableMetadata tableSchema) {
        try {
            StorageConfig config = StorageConfig.defaults();

            PartWriter writer = new PartWriter(
                    insertStmt.getTableName(),
                    tableSchema.columns(),
                    tableSchema.getConstraints(),
                    databasePath,
                    config
            );

            writer.writeRows(insertStmt.getValues());

        } catch (IOException e) {
            throw new RuntimeException("Failed to insert data: " + e.getMessage(), e);
        }
    }

}
