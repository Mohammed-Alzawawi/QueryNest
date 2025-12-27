package com.example.querynest.api.dto;
import com.example.querynest.ast.SelectStatement;

public class SelectQueryResponse {
    private String status;
    private SelectStatement ast;

    public SelectQueryResponse() {}

    public SelectQueryResponse(String status, SelectStatement ast) {
        this.status = status;
        this.ast = ast;
    }

    public String getStatus() { return status; }
    public SelectStatement getAst() { return ast; }
}
