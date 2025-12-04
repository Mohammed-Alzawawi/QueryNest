package com.example.querynest.query.impl;

import com.example.querynest.query.QueryCategory;
import com.example.querynest.query.QueryGuesser;
import org.springframework.stereotype.Component;

@Component
public class SimpleQueryGuesser implements QueryGuesser {

    @Override
    public QueryCategory guess(String rawQuery) {
        if (rawQuery == null) {
            return QueryCategory.UNKNOWN;
        }

        String trimmed = rawQuery.trim();
        if (trimmed.isEmpty()) {
            return QueryCategory.UNKNOWN;
        }

        String firstWord = trimmed.split("\\s+")[0].toUpperCase();

        return switch (firstWord) {
            case "CREATE", "ALTER", "DROP" -> QueryCategory.DDL;
            case "SELECT", "INSERT", "UPDATE", "DELETE" -> QueryCategory.DML;
            default -> QueryCategory.UNKNOWN;
        };
    }
}
