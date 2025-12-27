package com.example.querynest.api.dto;

import com.example.querynest.ast.CreateTableStatement;

import java.util.List;

public class CreateTableQueryResponse {
    private String status;
    private CreateTableStatement ast;
    private List<String> warnings;

    public CreateTableQueryResponse() {
    }

    public CreateTableQueryResponse(String status,
                                    CreateTableStatement ast,
                                    List<String> warnings) {
        this.status = status;
        this.ast = ast;
        this.warnings = warnings;
    }

    public String getStatus() {
        return status;
    }

    public CreateTableStatement getAst() {
        return ast;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}

