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
import java.nio.file.StandardCopyOption;
import java.util.*;

@Component
public class JsonTableSchemaSerializer implements TableSchemaSerializer {

    private final ObjectMapper objectMapper;

    public JsonTableSchemaSerializer() {
        this.objectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void serialize(CreateTableStatement statement, Path tableDir,  String uuid) throws IOException {
        Files.createDirectories(tableDir);
        Path schemaFile = tableDir.resolve("schema.json");

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", 1);

        Map<String, Object> tableInfo = new LinkedHashMap<>();
        tableInfo.put("name", statement.tableName().toLowerCase());
        tableInfo.put("engine", "FileSystem");
        tableInfo.put("orderBy", statement.orderBy());
        tableInfo.put("uuid", uuid);
        root.put("table", tableInfo);

        List<Map<String, Object>> columns = new ArrayList<>();
        for (ColumnDefinition col : statement.columns()) {
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("name", col.getName());

            Map<String, Object> typeObj = new LinkedHashMap<>();
            typeObj.put("name", col.getDataType());
            c.put("type", typeObj);

            c.put("nullable", col.isNullable());
            c.put("default", col.getDefaultValue());

            columns.add(c);
        }
        root.put("columns", columns);

        Map<String, Object> constraints = new LinkedHashMap<>();

        List<String> pk = new ArrayList<>();
        List<List<String>> uniques = new ArrayList<>();
        List<Map<String, Object>> fks = new ArrayList<>();

        for (Constraint constraint : statement.constraints()) {

            if (constraint instanceof PrimaryKeyConstraint pkc) {
                pk.addAll(pkc.columnNames());

            } else if (constraint instanceof UniqueConstraint uq) {
                uniques.add(uq.columnNames());

            } else if (constraint instanceof ForeignKeyConstraint fk) {
                Map<String, Object> fkObj = new LinkedHashMap<>();
                fkObj.put("columns", fk.columnNames());
                fkObj.put("referencedTable", fk.referencedTable());
                fkObj.put("referencedColumns", fk.referencedColumns());
                fks.add(fkObj);
            }
        }

        constraints.put("primaryKey", pk);
        constraints.put("unique", uniques);
        constraints.put("foreignKeys", fks);

        root.put("constraints", constraints);

        Path tempFile = tableDir.resolve("schema.json.tmp");
        objectMapper.writeValue(tempFile.toFile(), root);
        Files.move(
                tempFile,
                schemaFile,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
        );
    }
}
