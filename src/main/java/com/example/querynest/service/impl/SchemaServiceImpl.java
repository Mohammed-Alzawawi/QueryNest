package com.example.querynest.service.impl;

import com.example.querynest.ast.ColumnDefinition;
import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.builder.SqlGenerator;
import com.example.querynest.exception.ValidationException;
import com.example.querynest.parser.lexer.TokenNavigator;
import com.example.querynest.parser.lexer.Lexer;
import com.example.querynest.parser.statement.CreateTableBodyParser;
import com.example.querynest.schema.ColumnMetadata;
import com.example.querynest.schema.SchemaRegistry;
import com.example.querynest.schema.TableMetadata;
import com.example.querynest.service.SchemaService;
import com.example.querynest.validation.SchemaValidator;
import com.example.querynest.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchemaServiceImpl implements SchemaService {

    private final Lexer lexer;
    private final CreateTableBodyParser parser;
    private final SchemaValidator validator;
    private final SqlGenerator sqlGenerator;
    private final SchemaRegistry schemaRegistry;

    @Value("${database.storage.path}")
    private String databasePath;

    @Override
    public ValidationResult processCreateStatement(String rawStatement) {
        TokenNavigator nav = new TokenNavigator(lexer.tokenize(rawStatement));
        CreateTableStatement ast = parser.parse(nav);

        ValidationResult validationResult = validator.validate(ast);
        if (!validationResult.isValid()) {
            throw new ValidationException("Validation failed", validationResult.getErrors());
        }

        registerTable(ast);
        createTableOnFilesystem(ast);

        return validationResult;
    }

    @Override
    public CreateTableStatement parseStatement(String rawStatement) {
        TokenNavigator nav = new TokenNavigator(lexer.tokenize(rawStatement));
        return parser.parse(nav);
    }

    @Override
    public String generateSql(CreateTableStatement statement) {
        return sqlGenerator.generate(statement);
    }

    private void registerTable(CreateTableStatement statement) {
        List<ColumnMetadata> columns = statement.columns().stream()
                .map(col -> new ColumnMetadata(col.name(), col.dataType(), col.isNullable()))
                .toList();
        schemaRegistry.registerTable(new TableMetadata(statement.tableName(), columns));
    }

    private void createTableOnFilesystem(CreateTableStatement statement) {
        try {
            Path tableDir = setupTableDirectories(statement.tableName());
            Path schemaFile = tableDir.resolve("schema.bin");
            writeSchemaBinary(schemaFile, statement);

            Path dataDir = tableDir.resolve("data");
            Files.createDirectories(dataDir);

            long block = nextBlockNumber(tableDir);
            String partName = generatePartName(block);
            Path partDir = dataDir.resolve(partName);
            Files.createDirectories(partDir);

            for (ColumnDefinition col : statement.columns()) {
                Files.createFile(partDir.resolve(col.name() + ".bin"));
                Files.createFile(partDir.resolve(col.name() + ".mrk3"));

                if (col.isNullable()) {
                    Files.createFile(partDir.resolve(col.name() + ".null.bin"));
                    Files.createFile(partDir.resolve(col.name() + ".null.mrk3"));
                }
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeSchemaBinary(Path file, CreateTableStatement statement) throws IOException {
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(file))) {
            byte[] nameBytes = statement.tableName().getBytes(StandardCharsets.UTF_8);
            out.writeInt(nameBytes.length);
            out.write(nameBytes);

            List<ColumnDefinition> cols = statement.columns();
            out.writeInt(cols.size());

            for (ColumnDefinition col : cols) {
                byte[] n = col.name().getBytes(StandardCharsets.UTF_8);
                out.writeInt(n.length);
                out.write(n);

                byte[] t = col.dataType().getBytes(StandardCharsets.UTF_8);
                out.writeInt(t.length);
                out.write(t);

                out.writeBoolean(col.isNullable());
            }
        }
    }

    private Path setupTableDirectories(String tableName) throws IOException {
        Path dbRoot = Paths.get(databasePath).toAbsolutePath();
        Files.createDirectories(dbRoot);

        Path tableDir = dbRoot.resolve(tableName);
        Files.createDirectories(tableDir);

        return tableDir;
    }
    private long nextBlockNumber(Path tableDir) throws IOException {
        Path blockFile = tableDir.resolve("last_block.txt");

        long block = 1;

        if (Files.exists(blockFile)) {
            block = Long.parseLong(Files.readString(blockFile).trim()) + 1;
        }

        Files.writeString(blockFile, Long.toString(block), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        return block;
    }
    private String generatePartName(long block) {
        return block + "_" + block + "_0";
    }
}
