package com.example.querynest.service.impl;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.builder.SqlGenerator;
import com.example.querynest.exception.ValidationException;
import com.example.querynest.parser.lexer.Lexer;
import com.example.querynest.parser.lexer.Token;
import com.example.querynest.parser.lexer.TokenNavigator;
import com.example.querynest.parser.statement.CreateTableBodyParser;
import com.example.querynest.service.SchemaService;
import com.example.querynest.storage.StorageService;
import com.example.querynest.validation.SchemaValidator;
import com.example.querynest.validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchemaServiceImpl implements SchemaService {

    private final Lexer lexer;
    private final CreateTableBodyParser createTableBodyParser;
    private final SchemaValidator schemaValidator;
    private final SqlGenerator sqlGenerator;
    private final StorageService storageService;


    public SchemaServiceImpl(Lexer lexer, CreateTableBodyParser createTableBodyParser,
                             SchemaValidator schemaValidator,
                             SqlGenerator sqlGenerator, StorageService storageService) {
        this.lexer = lexer;
        this.createTableBodyParser = createTableBodyParser;
        this.schemaValidator = schemaValidator;
        this.sqlGenerator = sqlGenerator;
        this.storageService = storageService;
    }

    @Override
    public ValidationResult processCreateStatement(String rawStatement) {
        CreateTableStatement stmt = parseStatement(rawStatement);
        ValidationResult result = schemaValidator.validate(stmt);
        if (result.isValid()) {
            storageService.createTable(stmt);
        }
        return result;
    }

    @Override
    public CreateTableStatement parseStatement(String rawStatement) {
        List<Token> tokens = lexer.tokenize(rawStatement);
        TokenNavigator navigator = new TokenNavigator(tokens);
        return createTableBodyParser.parse(navigator);
    }

    @Override
    public String generateSql(CreateTableStatement statement) {
        return sqlGenerator.generate(statement);
    }
}
