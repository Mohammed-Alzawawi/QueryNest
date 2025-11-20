package com.example.querynest.storage.filesystem;

import com.example.querynest.ast.ColumnDefinition;
import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.ast.constraints.Constraint;
import com.example.querynest.ast.constraints.ForeignKeyConstraint;
import com.example.querynest.ast.constraints.PrimaryKeyConstraint;
import com.example.querynest.ast.constraints.UniqueConstraint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class JsonTableSchemaSerializer implements TableSchemaSerializer {

    private final ObjectMapper objectMapper;

    public JsonTableSchemaSerializer() {
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void serialize(CreateTableStatement statement, Path tableDir) throws IOException {
        Files.createDirectories(tableDir);
        Path schemaFile = tableDir.resolve("schema.json");

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("tableName", statement.tableName());
        root.put("orderBy", statement.orderBy());

        List<Map<String, Object>> columns = new ArrayList<>();
        for (ColumnDefinition col : statement.columns()) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("name", col.name());
            c.put("dataType", col.dataType());
            c.put("nullable", col.isNullable());
            c.put("defaultValue", col.defaultValue());
            columns.add(c);
        }
        root.put("columns", columns);

        List<Map<String, Object>> constraints = new ArrayList<>();
        for (Constraint constraint : statement.constraints()) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("name", constraint.name());

            if (constraint instanceof PrimaryKeyConstraint pk) {
                c.put("type", "PRIMARY_KEY");
                c.put("columns", pk.columnNames());
            } else if (constraint instanceof UniqueConstraint uq) {
                c.put("type", "UNIQUE");
                c.put("columns", uq.columnNames());
            } else if (constraint instanceof ForeignKeyConstraint fk) {
                c.put("type", "FOREIGN_KEY");
                c.put("columns", fk.columnNames());
                c.put("referencedTable", fk.referencedTable());
                c.put("referencedColumns", fk.referencedColumns());
            } else {
                c.put("type", "UNKNOWN");
            }

            constraints.add(c);
        }
        root.put("constraints", constraints);

        objectMapper.writeValue(schemaFile.toFile(), root);
    }
}
