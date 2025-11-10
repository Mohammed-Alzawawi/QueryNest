package com.example.querynest.parser.utils;

import com.example.querynest.parser.lexer.TokenType;

import java.util.Map;

public final class Keywords {
    private Keywords() {}

    public static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("CREATE", TokenType.CREATE),
            Map.entry("TABLE", TokenType.TABLE),
            Map.entry("PRIMARY", TokenType.PRIMARY),
            Map.entry("KEY", TokenType.KEY),
            Map.entry("FOREIGN", TokenType.FOREIGN),
            Map.entry("REFERENCES", TokenType.REFERENCES),
            Map.entry("UNIQUE", TokenType.UNIQUE),
            Map.entry("NOT", TokenType.NOT),
            Map.entry("NULL", TokenType.NULL),
            Map.entry("DEFAULT", TokenType.DEFAULT),
            Map.entry("CONSTRAINT", TokenType.CONSTRAINT),
            Map.entry("VARCHAR", TokenType.VARCHAR),
            Map.entry("INTEGER", TokenType.INTEGER),
            Map.entry("BIGINT", TokenType.BIGINT),
            Map.entry("BOOLEAN", TokenType.BOOLEAN),
            Map.entry("DATE", TokenType.DATE),
            Map.entry("TIMESTAMP", TokenType.TIMESTAMP),
            Map.entry("DECIMAL", TokenType.DECIMAL),
            Map.entry("TEXT", TokenType.TEXT),
            Map.entry("ORDER", TokenType.ORDER),
            Map.entry("BY", TokenType.BY)
    );
}
