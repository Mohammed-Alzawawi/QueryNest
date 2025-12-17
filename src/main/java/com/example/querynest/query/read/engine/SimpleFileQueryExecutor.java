package com.example.querynest.query.read.engine;

import com.example.querynest.query.read.api.*;
import com.example.querynest.query.read.parts.*;
import com.example.querynest.query.read.reader.SimplePartScanner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SimpleFileQueryExecutor implements FileQueryExecutor {

    private final PartManager partManager;
    private final SimplePartScanner scanner;

    public SimpleFileQueryExecutor(
            PartManager partManager,
            SimplePartScanner scanner
    ) {
        this.partManager = partManager;
        this.scanner = scanner;
    }

    @Override
    public QueryResult execute(SelectRequest request) {

        List<ResultRow> rows = new ArrayList<>();

        for (DataPart part : partManager.getActiveParts(request.tableName())) {

            List<ResultRow> partRows =
                    scanner.scanPart(part, request);

            for (ResultRow r : partRows) {
                rows.add(r);
                if (request.limit() != null &&
                        rows.size() >= request.limit()) {
                    return new QueryResult(rows);
                }
            }
        }
        return new QueryResult(rows);
    }
}
