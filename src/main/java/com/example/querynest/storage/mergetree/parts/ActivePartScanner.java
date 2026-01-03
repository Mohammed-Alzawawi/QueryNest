package com.example.querynest.storage.mergetree.parts;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ActivePartScanner {

    private final PartNameParser parser = new PartNameParser();
    private final PartValidator validator = new PartValidator();

    public List<MergeTreePart> scanAllParts(Path tableDir) throws java.io.IOException {
        Path partsDir = tableDir.resolve("parts");
        if (!Files.exists(partsDir) || !Files.isDirectory(partsDir)) {
            return List.of();
        }

        List<MergeTreePart> result = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(partsDir)) {
            for (Path partDir : stream) {
                if (!Files.isDirectory(partDir)) continue;

                String name = partDir.getFileName().toString();
                if (name.endsWith("_tmp")) continue;

                Optional<PartNameParser.ParsedPartName> parsed =
                        parser.tryParse(name);
                if (parsed.isEmpty()) continue;

                long rows = readRows(partDir);
                PartNameParser.ParsedPartName meta = parsed.get();

                MergeTreePart part = new MergeTreePart(
                        name,
                        partDir,
                        meta.getMinBlock(),
                        meta.getMaxBlock(),
                        meta.getLevel(),
                        rows
                );

                try {
                    validator.validate(part);
                    result.add(part);
                } catch (IllegalStateException e) {
                }
            }
        }

        return result;
    }

    private long readRows(Path partDir) {
        try {
            String s = Files.readString(
                    partDir.resolve("count.txt"),
                    StandardCharsets.UTF_8
            ).trim();
            return Math.max(0L, Long.parseLong(s));
        } catch (Exception e) {
            return 0L;
        }
    }
}