package com.example.querynest.api.dto;

import java.util.List;

public class ErrorResponse {
    private String status = "error";
    private String message;
    private List<String> errors;

    public ErrorResponse() {}
    public ErrorResponse(String message) { this.message = message; }
    public ErrorResponse(String message, List<String> errors) {
        this.message = message;
        this.errors = errors;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<String> getErrors() { return errors; }
}
