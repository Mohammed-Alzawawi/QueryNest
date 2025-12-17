package com.example.querynest.query.read.engine;

import com.example.querynest.query.read.api.QueryResult;
import com.example.querynest.query.read.api.SelectRequest;

public interface FileQueryExecutor {
    QueryResult execute(SelectRequest request);
}
