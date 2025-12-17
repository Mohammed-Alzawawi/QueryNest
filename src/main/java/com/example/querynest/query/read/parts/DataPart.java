package com.example.querynest.query.read.parts;

import java.nio.file.Path;

public record DataPart(
        String name,
        Path path,
        long minBlock,
        long maxBlock,
        int level
) {}
