package com.example.querynest.ast;

import java.util.List;
import java.util.Map;

public class InsertStatement {
    private final String tableName;
    private final List<String> columns;
    private final List<Map<String, Object>> values;

    public InsertStatement(String tableName, List<String> columns,
                           List<Map<String, Object>> values) {
        this.tableName = tableName;
        this.columns = columns;
        this.values = values;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Map<String, Object>> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "InsertStatement{" +
                "tableName='" + tableName + '\'' +
                ", columns=" + columns +
                ", values=" + values +
                '}';
    }
}
