package com.example.querynest.storage.filesystem;

import com.example.querynest.schema.ColumnMetadata;
import com.example.querynest.schema.TableMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonTableSchemaDeserializer {

    public static TableMetadata toTableMetadata(Map<String, Object> json) {

        Map<String, Object> tableBlock =
                (Map<String, Object>) json.get("table");

        String tableName = (String) tableBlock.get("name");
        String engine = (String) tableBlock.get("engine");
        String uuid = (String) tableBlock.get("uuid");

        List<Map<String, Object>> cols =
                (List<Map<String, Object>>) json.get("columns");

        List<ColumnMetadata> columnMetadata = new ArrayList<>();

        for (Map<String, Object> col : cols) {

            String name = (String) col.get("name");

            Map<String, Object> typeMap =
                    (Map<String, Object>) col.get("type");
            String dataType = (String) typeMap.get("name");

            boolean nullable = (Boolean) col.get("nullable");

            columnMetadata.add(
                    new ColumnMetadata(name, dataType, nullable)
            );
        }

        return new TableMetadata(
                tableName,
                columnMetadata,
                engine,
                uuid
        );
    }
}
