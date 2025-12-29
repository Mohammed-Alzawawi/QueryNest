package com.example.querynest.storage.cleanup;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

public class StartupCleanupService {

    private final String databasePath;

    public StartupCleanupService(String databasePath) {
        this.databasePath = databasePath;
    }

    public void cleanupOldParts() throws IOException {
        Path partsDir = Paths.get(databasePath, "parts");
        if (!Files.exists(partsDir)) return;

        try (Stream<Path> files = Files.list(partsDir)) {
            files.filter(Files::isDirectory)
                    .forEach(dir -> {
                        try {
                            Files.walk(dir)
                                    .sorted(Comparator.reverseOrder())
                                    .forEach(path -> {
                                        try { Files.delete(path); } catch (IOException ignored) {}
                                    });
                        } catch (IOException ignored) {}
                    });
        }
    }
}
