package com.example.querynest.api.dto;

import java.util.List;

public class ValidationResponse {
    private String status;
    private List<String> warnings;

    public ValidationResponse() {}
    public ValidationResponse(String status, List<String> warnings) {
        this.status = status;
        this.warnings = warnings;
    }

    public String getStatus() { return status; }
    public List<String> getWarnings() { return warnings; }
}
