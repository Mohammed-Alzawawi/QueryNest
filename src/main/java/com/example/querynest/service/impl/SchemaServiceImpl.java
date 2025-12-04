package com.example.querynest.service.impl;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.builder.SqlGenerator;
import com.example.querynest.exception.ValidationException;
import com.example.querynest.parser.lexer.Lexer;
import com.example.querynest.parser.lexer.Token;
import com.example.querynest.parser.lexer.TokenNavigator;
import com.example.querynest.parser.statement.CreateTableBodyParser;
import com.example.querynest.schema.ColumnMetadata;
import com.example.querynest.schema.SchemaRegistry;
import com.example.querynest.schema.TableMetadata;
import com.example.querynest.service.SchemaService;
import com.example.querynest.validation.SchemaValidator;
import com.example.querynest.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchemaServiceImpl implements SchemaService {

    private final Lexer lexer;
    private final CreateTableBodyParser createTableBodyParser;
    private final SchemaValidator schemaValidator;
    private final SchemaRegistry schemaRegistry;
    private final SqlGenerator sqlGenerator;

    @Override
    public ValidationResult processCreateStatement(String rawStatement) {
        CreateTableStatement stmt = parseStatement(rawStatement);

        ValidationResult result = schemaValidator.validate(stmt);
        if (!result.isValid()) {
            throw new ValidationException("Validation failed", result.getErrors());
        }

        List<ColumnMetadata> cols = stmt.columns().stream()
                .map(c -> new ColumnMetadata(
                        c.name(),
                        c.dataType(),
                        c.isNullable()
                ))
                .toList();

        TableMetadata metadata = new TableMetadata(stmt.tableName(), cols);
        schemaRegistry.registerTable(metadata);

        return result;
    }

    @Override
    public CreateTableStatement parseStatement(String rawStatement) {
        List<Token> tokens = lexer.tokenize(rawStatement);
        TokenNavigator nav = new TokenNavigator(tokens);
        return createTableBodyParser.parse(nav);
    }

    @Override
    public String generateSql(CreateTableStatement statement) {
        return sqlGenerator.generate(statement);
    }
}
