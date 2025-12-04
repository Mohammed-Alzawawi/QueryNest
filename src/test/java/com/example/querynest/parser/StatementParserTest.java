package com.example.querynest.parser;

import com.example.querynest.parser.lexer.Lexer;
import com.example.querynest.parser.lexer.LexerImpl;
import com.example.querynest.parser.statement.CreateTableBodyParser;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.parser.lexer.TokenNavigator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StatementParserTest {

    @Test
    void testParseCreateTable() {
        Lexer lexer = new LexerImpl();

        String sql = "CREATE TABLE users (id INTEGER, name VARCHAR(50));";

        TokenNavigator nav = new TokenNavigator(lexer.tokenize(sql));

        CreateTableBodyParser parser = new CreateTableBodyParser();

        CreateTableStatement stmt = parser.parse(nav);

        Assertions.assertEquals("users", stmt.tableName());
        Assertions.assertEquals(2, stmt.columns().size());
        Assertions.assertEquals("ID", stmt.columns().get(0).name().toUpperCase());
        Assertions.assertEquals("INTEGER", stmt.columns().get(0).dataType().toUpperCase());
    }
}
