package com.example.querynest.query.read.reader;

import com.example.querynest.query.read.api.*;
import com.example.querynest.query.read.parts.DataPart;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class SimplePartScanner {

    public List<ResultRow> scanPart(
            DataPart part,
            SelectRequest request
    ) {

        // Placeholder: assume CSV for now
        Path dataFile = part.path().resolve("data.csv");

        if (!Files.exists(dataFile)) {
            return List.of();
        }

        List<ResultRow> result = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(dataFile);
            String[] headers = lines.get(0).split(",");

            for (int i = 1; i < lines.size(); i++) {

                String[] values = lines.get(i).split(",");
                Map<String, Object> row = new HashMap<>();

                for (int c = 0; c < headers.length; c++) {
                    String col = headers[c];
                    if (request.projectedColumns().contains(col)) {
                        row.put(col, values[c]);
                    }
                }
                result.add(new ResultRow(row));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed reading part " + part.name(), e);
        }
        return result;
    }
}
