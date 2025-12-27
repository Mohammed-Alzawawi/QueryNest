package com.example.querynest.storage.filesystem;

import com.example.querynest.ast.ColumnDefinition;
import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.schema.ColumnMetadata;
import com.example.querynest.schema.SchemaRegistry;
import com.example.querynest.schema.TableMetadata;
import com.example.querynest.storage.StorageService;
import com.example.querynest.storage.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FileSystemStorageService implements StorageService {

    private final SchemaRegistry schemaRegistry;
    private final TableSchemaSerializer schemaSerializer;

    @Value("${querynest.storage.base-path:./data}")
    private String basePath;

    private final Object tableCreationLock = new Object();

    public FileSystemStorageService(SchemaRegistry schemaRegistry,
                                    TableSchemaSerializer schemaSerializer) {
        this.schemaRegistry = schemaRegistry;
        this.schemaSerializer = schemaSerializer;
    }

    @Override
    public void createTable(CreateTableStatement statement) {
        String tableName = statement.tableName().toLowerCase();
        Path tableDir = null;
        synchronized (tableCreationLock) {
            try {
                if (schemaRegistry.getTable(tableName) != null) {
                    throw new StorageException("Table '" + tableName + "' is already registered in the schema registry");
                }

                Path rootDir = Paths.get(basePath).toAbsolutePath().normalize();
                Files.createDirectories(rootDir);

                tableDir = rootDir.resolve(tableName);

                if (Files.exists(tableDir)) {
                    throw new StorageException("Table directory already exists on disk: " + tableDir);
                }

                Files.createDirectories(tableDir);

                String uuid = UUID.randomUUID().toString();
                schemaSerializer.serialize(statement, tableDir, uuid);

                TableMetadata metadata = toTableMetadata(statement, uuid);
                schemaRegistry.registerTable(metadata);

            } catch (IOException e) {
                if (tableDir != null && Files.exists(tableDir)) {
                    try {
                        Files.walk(tableDir)
                                .sorted((a, b) -> b.compareTo(a))
                                .forEach(path -> {
                                    try {
                                        Files.delete(path);
                                    } catch (IOException ignored) {
                                    }
                                });
                    } catch (IOException ignored) {
                    }
                }
                throw new StorageException("Failed to create table '" + tableName + "'", e);

            }
        }
    }

    private TableMetadata toTableMetadata(CreateTableStatement statement, String uuid) {
        List<ColumnMetadata> columns = statement.columns().stream()
                .map(this::toColumnMetadata)
                .toList();

        return new TableMetadata(
                statement.tableName(),
                columns,
                statement.engine(),
                uuid
        );
    }

    private ColumnMetadata toColumnMetadata(ColumnDefinition col) {
        return new ColumnMetadata(
                col.name(),
                col.dataType(),
                col.isNullable()
        );
    }

    @Override
    public List<String> listTables() {
        return schemaRegistry.getAllTables().stream()
                .map(TableMetadata::name)
                .toList();
    }

    @Override
    public TableMetadata describeTable(String tableName) {
        TableMetadata metadata = schemaRegistry.getTable(tableName.toLowerCase());

        if (metadata == null) {
            throw new StorageException("Table '" + tableName + "' does not exist");
        }

        return metadata;
    }
}