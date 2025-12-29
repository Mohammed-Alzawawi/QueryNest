package com.example.querynest.storage.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PartMetadata {

    @JsonProperty("table_name")
    private String tableName;

    @JsonProperty("row_count")
    private long rowCount;

    @JsonProperty("granule_count")
    private long granuleCount;

    @JsonProperty("index_granularity")
    private int indexGranularity;

    @JsonProperty("created_timestamp")
    private long createdTimestamp;

    @JsonProperty("columns")
    private Map<String, ColumnInfo> columns;

    public PartMetadata() {
        this.columns = new HashMap<>();
        this.createdTimestamp = System.currentTimeMillis();
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public void setGranuleCount(long granuleCount) {
        this.granuleCount = granuleCount;
    }

    public void setIndexGranularity(int indexGranularity) {
        this.indexGranularity = indexGranularity;
    }

    public void addColumn(String name, String dataType, boolean nullable,
                          String codec, int nullCount) {
        ColumnInfo info = new ColumnInfo();
        info.dataType = dataType;
        info.nullable = nullable;
        info.codec = codec;
        info.nullCount = nullCount;
        columns.put(name, info);
    }

    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsString(this);
    }

    public static class ColumnInfo {
        @JsonProperty("data_type")
        public String dataType;

        @JsonProperty("nullable")
        public boolean nullable;

        @JsonProperty("codec")
        public String codec;

        @JsonProperty("null_count")
        public int nullCount;
    }
}