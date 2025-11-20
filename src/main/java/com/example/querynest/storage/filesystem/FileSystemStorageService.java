package com.example.querynest.storage.filesystem;

import com.example.querynest.ast.ColumnDefinition;
import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.schema.ColumnMetadata;
import com.example.querynest.schema.SchemaRegistry;
import com.example.querynest.schema.TableMetadata;
import com.example.querynest.storage.StorageService;
import com.example.querynest.storage.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileSystemStorageService implements StorageService {

    private final SchemaRegistry schemaRegistry;
    private final TableSchemaSerializer schemaSerializer;

    @Value("${querynest.storage.base-path:./data}")
    private String basePath;

    @Override
    public void createTable(CreateTableStatement statement) {
        String tableName = statement.tableName();

        try {
            if (schemaRegistry.getTable(tableName) != null) {
                throw new StorageException("Table '" + tableName + "' is already registered in the schema registry");
            }

            Path rootDir = Paths.get(basePath).toAbsolutePath().normalize();
            Files.createDirectories(rootDir);

            Path tableDir = rootDir.resolve(tableName.toLowerCase());
            if (Files.exists(tableDir)) {
                throw new StorageException("Table directory already exists on disk: " + tableDir);
            }

            Files.createDirectories(tableDir);

            schemaSerializer.serialize(statement, tableDir);

            TableMetadata metadata = toTableMetadata(statement);
            schemaRegistry.registerTable(metadata);

        } catch (IOException e) {
            throw new StorageException("Failed to create table '" + tableName + "'", e);
        }
    }

    private TableMetadata toTableMetadata(CreateTableStatement statement) {
        List<ColumnMetadata> columns = statement.columns().stream()
                .map(this::toColumnMetadata)
                .toList();

        return new TableMetadata(statement.tableName(), columns);
    }

    private ColumnMetadata toColumnMetadata(ColumnDefinition col) {
        return new ColumnMetadata(
                col.name(),
                col.dataType(),
                col.isNullable()
        );
    }
}
