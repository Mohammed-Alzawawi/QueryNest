package com.example.querynest.storage.filesystem;

import com.example.querynest.schema.TableMetadata;
import com.example.querynest.schema.SchemaRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class SchemaLoader {

    private static final Logger log = LoggerFactory.getLogger(SchemaLoader.class);

    private final SchemaRegistry schemaRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${querynest.storage.base-path:./data}")
    private String basePath;

    public SchemaLoader(SchemaRegistry schemaRegistry) {
        this.schemaRegistry = schemaRegistry;
    }

    @PostConstruct
    public void loadSchemas() {
        try {
            Path rootDir = Paths.get(basePath).toAbsolutePath().normalize();

            if (!Files.exists(rootDir)) {
                log.warn("SchemaLoader: Storage root directory not found: {}", rootDir);
                return;
            }

            Files.list(rootDir)
                    .filter(Files::isDirectory)
                    .forEach(tableDir -> {
                        try {
                            Path schemaFile = tableDir.resolve("schema.json");

                            if (!Files.exists(schemaFile)) {
                                log.warn("SchemaLoader: No schema.json found in {}", tableDir);
                                return;
                            }

                            Map<String, Object> json =
                                    objectMapper.readValue(schemaFile.toFile(), Map.class);

                            TableMetadata metadata =
                                    JsonTableSchemaDeserializer.toTableMetadata(json);

                            schemaRegistry.registerTable(metadata);

                            log.info("Loaded table '{}' (UUID={}) from disk.",
                                    metadata.name(),
                                    metadata.uuid());

                        } catch (Exception e) {
                            log.error("Failed to load schema in {}", tableDir, e);
                        }
                    });

        } catch (IOException e) {
            log.error("SchemaLoader: Error scanning base directory", e);
        }
    }
}
