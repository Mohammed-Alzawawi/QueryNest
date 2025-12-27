package com.example.querynest.parser.lexer;

public record Token(
        TokenType type,
        String value,
        int position
) {}
