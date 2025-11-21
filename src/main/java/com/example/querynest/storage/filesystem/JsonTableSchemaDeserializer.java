package com.example.querynest.storage.filesystem;

import com.example.querynest.schema.ColumnMetadata;
import com.example.querynest.schema.TableMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonTableSchemaDeserializer {

    public static TableMetadata toTableMetadata(Map<String, Object> json) {
        String tableName = (String) json.get("tableName");

        List<Map<String, Object>> cols =
                (List<Map<String, Object>>) json.get("columns");

        List<ColumnMetadata> columnMetadata = new ArrayList<>();

        for (Map<String, Object> col : cols) {
            String name = (String) col.get("name");
            String dataType = (String) col.get("dataType");
            boolean nullable = (Boolean) col.get("nullable");

            columnMetadata.add(new ColumnMetadata(name, dataType, nullable));
        }

        return new TableMetadata(tableName, columnMetadata);
    }
}
