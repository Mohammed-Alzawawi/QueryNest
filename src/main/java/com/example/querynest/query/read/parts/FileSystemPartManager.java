package com.example.querynest.query.read.parts;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FileSystemPartManager implements PartManager {

    private static final Pattern PART_PATTERN =
            Pattern.compile("(\\d+)_(\\d+)_(\\d+)");

    @Value("${querynest.storage.base-path:./data}")
    private String basePath;

    @Override
    public List<DataPart> getActiveParts(String tableName) {

        Path partsDir = Paths.get(basePath, tableName, "parts");

        if (!Files.exists(partsDir)) {
            return List.of();
        }

        List<DataPart> allParts = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(partsDir)) {
            for (Path dir : stream) {
                String name = dir.getFileName().toString();

                if (name.endsWith("_tmp")) continue;

                Matcher m = PART_PATTERN.matcher(name);
                if (!m.matches()) continue;

                long min = Long.parseLong(m.group(1));
                long max = Long.parseLong(m.group(2));
                int level = Integer.parseInt(m.group(3));

                allParts.add(new DataPart(name, dir, min, max, level));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to list parts", e);
        }

        return resolveOverlaps(allParts);
    }

    private List<DataPart> resolveOverlaps(List<DataPart> parts) {

        parts.sort(Comparator.comparingInt(DataPart::level).reversed());

        List<DataPart> active = new ArrayList<>();
        Set<Long> covered = new HashSet<>();

        for (DataPart part : parts) {
            boolean overlaps = false;
            for (long b = part.minBlock(); b <= part.maxBlock(); b++) {
                if (covered.contains(b)) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) {
                active.add(part);
                for (long b = part.minBlock(); b <= part.maxBlock(); b++) {
                    covered.add(b);
                }
            }
        }
        return active;
    }
}
