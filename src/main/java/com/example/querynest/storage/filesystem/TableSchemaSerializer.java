package com.example.querynest.storage.filesystem;

import com.example.querynest.ast.CreateTableStatement;

import java.io.IOException;
import java.nio.file.Path;

public interface TableSchemaSerializer {
    void serialize(CreateTableStatement statement, Path tableDir,  String uuid) throws IOException;
}
