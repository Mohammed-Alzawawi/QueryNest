package com.example.querynest.query.impl;

import com.example.querynest.api.dto.SelectQueryResponse;
import com.example.querynest.query.QueryProcessor;
import com.example.querynest.service.DmlService;
import org.springframework.stereotype.Component;

@Component
public class DmlProcessor extends QueryProcessor {
    private final DmlService dmlService;

    public DmlProcessor(DmlService dmlService) {
        this.dmlService = dmlService;
    }

    @Override
    protected Object doProcess(String query) {
        // select/insert/delete/update
        String upper = query.toUpperCase();
        if (upper.startsWith("SELECT")) {
            var ast = dmlService.parseSelect(query);
            return new SelectQueryResponse("success", ast);
        }

        throw new UnsupportedOperationException("Only SELECT DML statement is supported currently.");
    }
}
