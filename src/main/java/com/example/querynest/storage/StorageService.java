package com.example.querynest.storage;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.schema.TableMetadata;

import java.util.List;

public interface StorageService {

    void createTable(CreateTableStatement statement);
    List<String> listTables();
    TableMetadata describeTable(String tableName);
//    CreateTableStatement getTableSchema(String tableName);

}

