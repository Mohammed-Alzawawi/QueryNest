package com.example.querynest.storage.mergetree.parts;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class PartHandleRegistry {

    private final ConcurrentHashMap<Path, PartHandle> handles = new ConcurrentHashMap<>();

    public PartHandle getOrCreate(MergeTreePart part) {
        Objects.requireNonNull(part, "part");

        return handles.computeIfAbsent(
                part.getPath(),
                p -> new PartHandle(part)
        );
    }

    public void remove(MergeTreePart part) {
        if (part == null) return;
        handles.remove(part.getPath());
    }

    public Collection<PartHandle> all() {
        return handles.values();
    }
}