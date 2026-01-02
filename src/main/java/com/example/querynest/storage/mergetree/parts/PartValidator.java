package com.example.querynest.storage.mergetree.parts;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PartValidator {

    private final PartNameParser nameParser = new PartNameParser();

    public void validate(MergeTreePart part) {
        if (part == null) {
            throw new IllegalStateException("part is null");
        }

        if (part.getName().isEmpty()) {
            throw new IllegalStateException("part name is empty");
        }

        if (part.getName().endsWith("_tmp")) {
            throw new IllegalStateException(
                    "temporary part cannot be validated: " + part.getName()
            );
        }

        nameParser.tryParse(part.getName())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "invalid part name format: " + part.getName()
                        )
                );

        if (part.getMinBlock() > part.getMaxBlock()) {
            throw new IllegalStateException(
                    "invalid block range in part: " + part.getName()
            );
        }

        if (part.getLevel() < 0) {
            throw new IllegalStateException(
                    "negative level in part: " + part.getName()
            );
        }

        if (part.getRows() < 0) {
            throw new IllegalStateException(
                    "negative row count in part: " + part.getName()
            );
        }

        Path path = part.getPath();
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new IllegalStateException(
                    "part directory does not exist: " + path
            );
        }

        Path count = path.resolve("count.txt");
        if (!Files.isRegularFile(count)) {
            throw new IllegalStateException(
                    "missing count.txt in part: " + part.getName()
            );
        }

        Path pk = path.resolve("primary.idx");
        if (!Files.isRegularFile(pk)) {
            throw new IllegalStateException(
                    "missing primary.idx in part: " + part.getName()
            );
        }

        boolean hasBin = false;
        boolean hasMrk = false;

        try (DirectoryStream<Path> files = Files.newDirectoryStream(path)) {
            for (Path f : files) {
                String n = f.getFileName().toString();
                if (n.endsWith(".bin")) hasBin = true;
                if (n.endsWith(".mrk3")) hasMrk = true;
                if (hasBin && hasMrk) break;
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "failed to scan part files: " + part.getName(),
                    e
            );
        }

        if (!hasBin || !hasMrk) {
            throw new IllegalStateException(
                    "part missing data or mark files: " + part.getName()
            );
        }
    }
}