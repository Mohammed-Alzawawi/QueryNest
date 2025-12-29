package com.example.querynest.parser.statement;

import com.example.querynest.ast.InsertStatement;
import com.example.querynest.parser.lexer.Token;
import com.example.querynest.parser.lexer.TokenNavigator;
import com.example.querynest.parser.lexer.TokenType;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InsertStatementParser {

    public InsertStatement parse(TokenNavigator navigator) {
        navigator.consume(TokenType.INSERT,"Expected INSERT");
        navigator.consume(TokenType.INTO, "Expected INTO");

        String tableName = navigator.consume(TokenType.IDENTIFIER, "Expected table name").value();

        List<String> columns = null;
        if (navigator.check(TokenType.LEFT_PAREN)) {
            columns = parseColumnList(navigator);
        }

        navigator.consume(TokenType.VALUES,"Expected Values");

        List<Map<String, Object>> values = parseValuesList(navigator, columns);

        return new InsertStatement(tableName, columns, values);
    }

    private List<String> parseColumnList(TokenNavigator navigator) {
        List<String> columns = new ArrayList<>();
        navigator.consume(TokenType.LEFT_PAREN, "Expected '('");

        do {
            columns.add(navigator.consume(TokenType.IDENTIFIER, "").value());
        } while (navigator.match(TokenType.COMMA));

        navigator.consume(TokenType.RIGHT_PAREN, "Expected ')'");
        return columns;
    }

    private List<Map<String, Object>> parseValuesList(TokenNavigator navigator,
                                                      List<String> columns) {
        List<Map<String, Object>> allValues = new ArrayList<>();

        do {
            navigator.consume(TokenType.LEFT_PAREN, "Expected '('");
            List<Object> rowValues = new ArrayList<>();

            do {
                Token token = navigator.advance();
                Object value = parseValue(token);
                rowValues.add(value);
            } while (navigator.match(TokenType.COMMA));

            navigator.consume(TokenType.RIGHT_PAREN, "Expected ')'");

            Map<String, Object> rowMap = new HashMap<>();
            for (int i = 0; i < rowValues.size(); i++) {
                String colName = (columns != null && i < columns.size())
                        ? columns.get(i)
                        : "col_" + i;
                rowMap.put(colName, rowValues.get(i));
            }

            allValues.add(rowMap);
        } while (navigator.match(TokenType.COMMA));

        return allValues;
    }

    private Object parseValue(Token token) {
        return switch (token.type()) {
            case NUMBER -> parseNumber(token.value());
            case STRING -> token.value();
            case IDENTIFIER -> {
                if ("NULL".equalsIgnoreCase(token.value())) {
                    yield null;
                }
                yield token.value();
            }
            default -> token.value();
        };
    }

    private Object parseNumber(String value) {
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            }
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }
}
