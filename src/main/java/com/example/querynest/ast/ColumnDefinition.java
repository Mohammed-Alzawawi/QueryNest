package com.example.querynest.ast;

public class ColumnDefinition {
    private final String name;
    private final String dataType;
    private final boolean nullable;
    private final String defaultValue;

    public ColumnDefinition(String name, String dataType, boolean nullable) {
        this(name, dataType, nullable, null);
    }

    public ColumnDefinition(String name, String dataType, boolean nullable,
                            String defaultValue) {
        this.name = name;
        this.dataType = dataType;
        this.nullable = nullable;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}