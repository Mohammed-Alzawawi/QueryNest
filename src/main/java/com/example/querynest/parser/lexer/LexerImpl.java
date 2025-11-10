package com.example.querynest.parser.lexer;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.example.querynest.parser.utils.Keywords.KEYWORDS;

@Component
public class LexerImpl implements Lexer {

    @Override
    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int position = 0;

        while (position < input.length()) {
            char current = input.charAt(position);

            if (Character.isWhitespace(current)) {
                position++;
                continue;
            }

            switch (current) {
                case '(' -> {
                    tokens.add(new Token(TokenType.LEFT_PAREN, "(", position));
                    position++;
                    continue;
                }
                case ')' -> {
                    tokens.add(new Token(TokenType.RIGHT_PAREN, ")", position));
                    position++;
                    continue;
                }
                case ',' -> {
                    tokens.add(new Token(TokenType.COMMA, ",", position));
                    position++;
                    continue;
                }
                case ';' -> {
                    tokens.add(new Token(TokenType.SEMICOLON, ";", position));
                    position++;
                    continue;
                }
            }

            if (current == '\'' || current == '"') {
                int start = position;
                position++;
                while (position < input.length() && input.charAt(position) != current) {
                    position++;
                }
                if (position < input.length()) position++;
                tokens.add(new Token(TokenType.STRING,
                        input.substring(start + 1, position - 1), start));
                continue;
            }

            if (Character.isDigit(current)) {
                int start = position;
                while (position < input.length() &&
                        (Character.isDigit(input.charAt(position)) ||
                                input.charAt(position) == '.')) {
                    position++;
                }
                tokens.add(new Token(TokenType.NUMBER,
                        input.substring(start, position), start));
                continue;
            }

            if (Character.isLetter(current) || current == '_') {
                int start = position;
                while (position < input.length() &&
                        (Character.isLetterOrDigit(input.charAt(position)) ||
                                input.charAt(position) == '_')) {
                    position++;
                }
                String word = input.substring(start, position);
                TokenType type = KEYWORDS.getOrDefault(
                        word.toUpperCase(), TokenType.IDENTIFIER);
                tokens.add(new Token(type, word, start));
                continue;
            }

            tokens.add(new Token(TokenType.UNKNOWN,
                    String.valueOf(current), position));
            position++;
        }

        tokens.add(new Token(TokenType.EOF, "", position));
        return tokens;
    }
}
