package com.example.querynest.builder;

import com.example.querynest.ast.CreateTableStatement;

public interface SqlGenerator {
    String generate(CreateTableStatement statement);
}
