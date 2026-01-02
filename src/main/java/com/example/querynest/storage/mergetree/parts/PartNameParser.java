package com.example.querynest.storage.mergetree.parts;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PartNameParser {

    private static final Pattern PATTERN =
            Pattern.compile("^(\\d+)_(\\d+)_(\\d+)$");

    public Optional<ParsedPartName> tryParse(String name) {
        Matcher m = PATTERN.matcher(name);
        if (!m.matches()) return Optional.empty();

        long min = Long.parseLong(m.group(1));
        long max = Long.parseLong(m.group(2));
        int level = Integer.parseInt(m.group(3));

        if (min > max || level < 0) return Optional.empty();

        return Optional.of(new ParsedPartName(min, max, level));
    }

    public static final class ParsedPartName {
        private final long minBlock;
        private final long maxBlock;
        private final int level;

        public ParsedPartName(long minBlock, long maxBlock, int level) {
            this.minBlock = minBlock;
            this.maxBlock = maxBlock;
            this.level = level;
        }

        public long getMinBlock() {
            return minBlock;
        }

        public long getMaxBlock() {
            return maxBlock;
        }

        public int getLevel() {
            return level;
        }
    }
}