package com.example.querynest.storage;

import com.example.querynest.ast.CreateTableStatement;

public interface StorageService {
    void createTable(CreateTableStatement statement);
}
