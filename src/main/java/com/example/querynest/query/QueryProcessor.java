package com.example.querynest.query;

public abstract class QueryProcessor {

    public Object process(String rawQuery) {
        String normalized = normalize(rawQuery);
        return doProcess(normalized);
    }

    protected String normalize(String rawQuery) {
        return rawQuery == null ? "" : rawQuery.trim();
    }

    protected abstract Object doProcess(String query);
}
