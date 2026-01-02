package com.example.querynest.storage.mergetree.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class ActivePartSnapshot {

    private final List<MergeTreePart> parts;

    public ActivePartSnapshot(List<MergeTreePart> discoveredParts,
                              PartValidator validator) {

        if (discoveredParts == null) {
            throw new IllegalStateException("discoveredParts is null");
        }
        if (validator == null) {
            throw new IllegalStateException("validator is null");
        }

        List<MergeTreePart> validated = new ArrayList<>();

        for (MergeTreePart part : discoveredParts) {
            validator.validate(part);
            validated.add(part);
        }

        validated.sort(
                Comparator.comparingLong(MergeTreePart::getMinBlock)
                        .thenComparingLong(MergeTreePart::getMaxBlock)
                        .thenComparing(
                                Comparator.comparingInt(MergeTreePart::getLevel).reversed()
                        )
        );

        this.parts = Collections.unmodifiableList(validated);
    }

    public List<MergeTreePart> getParts() {
        return parts;
    }

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    public int size() {
        return parts.size();
    }

    public MergeTreePart get(int index) {
        return parts.get(index);
    }

    @Override
    public String toString() {
        return "ActivePartSnapshot{parts=" + parts + '}';
    }
}