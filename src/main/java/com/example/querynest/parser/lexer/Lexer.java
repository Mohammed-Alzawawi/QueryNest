package com.example.querynest.parser.lexer;

import java.util.List;

public interface Lexer {
    List<Token> tokenize(String input);
}