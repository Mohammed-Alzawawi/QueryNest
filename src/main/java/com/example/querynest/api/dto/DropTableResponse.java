package com.example.querynest.api.dto;

public class DropTableResponse {
    private String status;
    private String tableName;
    private String message;

    public DropTableResponse() {
    }

    public DropTableResponse(String status, String tableName, String message) {
        this.status = status;
        this.tableName = tableName;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getTableName() {
        return tableName;
    }

    public String getMessage() {
        return message;
    }
}
