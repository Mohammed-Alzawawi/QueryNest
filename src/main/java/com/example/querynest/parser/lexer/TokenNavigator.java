package com.example.querynest.parser.lexer;

import java.util.List;

public class TokenNavigator {

    private final List<Token> tokens;
    private int current = 0;

    public TokenNavigator(List<Token> tokens) {
        this.tokens = tokens;
    }

    public boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    public Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw new RuntimeException(message + " at position " + peek().position());
    }

    public Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    public boolean match(TokenType... types) {
        for (TokenType t : types) {
            if (check(t)) {
                advance();
                return true;
            }
        }
        return false;
    }

    public boolean isAtEnd() {
        return peek().type() == TokenType.EOF;
    }

    public Token peek() {
        return tokens.get(current);
    }

    public Token previous() {
        return tokens.get(current - 1);
    }
}
