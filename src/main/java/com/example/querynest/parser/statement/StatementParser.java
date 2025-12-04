package com.example.querynest.parser.statement;

import com.example.querynest.ast.CreateTableStatement;

public interface StatementParser {
    CreateTableStatement parse(String statement);
}
