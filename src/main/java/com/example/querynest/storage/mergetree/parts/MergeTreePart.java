package com.example.querynest.storage.mergetree.parts;

import java.nio.file.Path;
import java.util.Objects;

public final class MergeTreePart {

    private final String name;
    private final Path path;
    private final long minBlock;
    private final long maxBlock;
    private final int level;
    private final long rows;

    public MergeTreePart(String name, Path path, long minBlock, long maxBlock, int level, long rows) {
        this.name = Objects.requireNonNull(name, "name");
        this.path = Objects.requireNonNull(path, "path");
        this.minBlock = minBlock;
        this.maxBlock = maxBlock;
        this.level = level;
        this.rows = rows;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
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

    public long getRows() {
        return rows;
    }

    public boolean overlaps(MergeTreePart other) {
        return this.minBlock <= other.maxBlock && other.minBlock <= this.maxBlock;
    }

    public boolean covers(MergeTreePart other) {
        return this.minBlock <= other.minBlock && this.maxBlock >= other.maxBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MergeTreePart)) return false;
        MergeTreePart that = (MergeTreePart) o;
        return name.equals(that.name) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }

    @Override
    public String toString() {
        return "MergeTreePart{" +
                "name='" + name + '\'' +
                ", minBlock=" + minBlock +
                ", maxBlock=" + maxBlock +
                ", level=" + level +
                ", rows=" + rows +
                '}';
    }
}