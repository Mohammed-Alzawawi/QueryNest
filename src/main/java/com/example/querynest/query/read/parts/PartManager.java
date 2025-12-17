package com.example.querynest.query.read.parts;

import java.util.List;

public interface PartManager {
    List<DataPart> getActiveParts(String tableName);
}
