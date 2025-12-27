package com.example.querynest.parser.statement;

import com.example.querynest.ast.SelectStatement;
import com.example.querynest.ast.filter.*;
import com.example.querynest.exception.ParseException;
import com.example.querynest.parser.lexer.TokenNavigator;
import com.example.querynest.parser.lexer.TokenType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class SelectStatementParser {

    public SelectStatement parse(TokenNavigator navigator) throws ParseException {
        navigator.consume(TokenType.SELECT, "Expected SELECT");

        List<String> projectedColumns = parseProjection(navigator);

        navigator.consume(TokenType.FROM, "Expected FROM");
        String tableName = navigator.consume(TokenType.IDENTIFIER, "Expected table name").value();

        List<FilterCondition> filters = new ArrayList<>();
        if (navigator.match(TokenType.WHERE)) {
            do {
                filters.add(parseCondition(navigator));
            } while (navigator.match(TokenType.AND));
            if (navigator.check(TokenType.OR)) {
                throw new ParseException("OR is not supported yet , use AND only");
            }
        }
        Integer limit = null;
        if (navigator.match(TokenType.LIMIT)) {
            String n = navigator.consume(TokenType.NUMBER, "Expected number after LIMIT").value();
            limit = (int) Double.parseDouble(n);
            if (limit < 0) throw new ParseException("LIMIT must be >= 0");
        }
        if (navigator.check(TokenType.SEMICOLON)) navigator.advance();

        return new SelectStatement(tableName, projectedColumns, filters, limit);
    }
    private List<String> parseProjection(TokenNavigator navigator) {
        List<String> cols = new ArrayList<>();

        if (navigator.match(TokenType.STAR)) {
            cols.add("*");
            return cols;
        }

        cols.add(navigator.consume(TokenType.IDENTIFIER, "Expected column in SELECT list").value());
        while (navigator.match(TokenType.COMMA)) {
            cols.add(navigator.consume(TokenType.IDENTIFIER, "Expected column after comma").value());
        }

        return cols;
    }
    private FilterCondition parseCondition(TokenNavigator navigator) {
        String column = navigator.consume(TokenType.IDENTIFIER, "Expected column name in WHERE").value();
        Operator op = parseOperator(navigator);
        Value value = parseValue(navigator);
        return new FilterCondition(column, op, value);
    }
    private Operator parseOperator(TokenNavigator navigator) {
        if (navigator.match(TokenType.EQUAL)) return Operator.EQ;
        if (navigator.match(TokenType.NOT_EQUAL)) return Operator.NEQ;
        if (navigator.match(TokenType.LESS)) return Operator.LT;
        if (navigator.match(TokenType.LESS_EQUAL)) return Operator.LTE;
        if (navigator.match(TokenType.GREATER)) return Operator.GT;
        if (navigator.match(TokenType.GREATER_EQUAL)) return Operator.GTE;

        throw new ParseException("Expected operator (=, !=, <, <=, >, >=)");
    }
    private Value parseValue(TokenNavigator navigator) {
        if (navigator.check(TokenType.STRING)) {
            return new StrValue(navigator.advance().value());
        }
        if (navigator.check(TokenType.NUMBER)) {
            return new NumValue(Double.parseDouble(navigator.advance().value()));
        }
        if (navigator.check(TokenType.IDENTIFIER)) {
            String v = navigator.advance().value();

            if (v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false")) {
                return new BoolValue(Boolean.parseBoolean(v));
            }

            throw new ParseException("Expected value (number/string/boolean) but got: " + v);
        }
        throw new ParseException("Expected value");
    }

}
