package com.example.querynest.query;

import com.example.querynest.query.impl.DdlProcessor;
import com.example.querynest.query.impl.DmlProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryProcessorFactory {

    private final QueryGuesser queryGuesser;
    private final DdlProcessor ddlProcessor;
    private final DmlProcessor dmlProcessor;

    public QueryProcessor getProcessor(String rawQuery) {
        QueryCategory category = queryGuesser.guess(rawQuery);

        return switch (category) {
            case DDL -> ddlProcessor;
            case DML -> dmlProcessor;
            case UNKNOWN -> throw new IllegalArgumentException("Unknown or unsupported query type");
        };
    }
}
