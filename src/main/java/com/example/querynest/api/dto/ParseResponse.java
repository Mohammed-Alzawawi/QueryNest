package com.example.querynest.api.dto;

import com.example.querynest.ast.CreateTableStatement;

public class ParseResponse {
    private String status;
    private CreateTableStatement ast;

    public ParseResponse() {}
    public ParseResponse(String status, CreateTableStatement ast) {
        this.status = status;
        this.ast = ast;
    }

    public String getStatus() { return status; }
    public CreateTableStatement getAst() { return ast; }
}
