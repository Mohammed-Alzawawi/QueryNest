package com.example.querynest.storage.mergetree.parts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PartOverlapResolver {

    public List<MergeTreePart> resolveActive(List<MergeTreePart> discovered) {
        if (discovered.isEmpty()) return List.of();

        List<MergeTreePart> parts = new ArrayList<>(discovered);

        parts.sort(
                Comparator.comparingLong(MergeTreePart::getMinBlock)
                        .thenComparingLong(MergeTreePart::getMaxBlock)
                        .thenComparing(Comparator.comparingInt(MergeTreePart::getLevel).reversed())
        );

        List<MergeTreePart> kept = new ArrayList<>();

        for (MergeTreePart candidate : parts) {
            boolean shadowed = false;

            for (MergeTreePart k : kept) {
                if (!k.overlaps(candidate)) continue;

                if (k.covers(candidate) && k.getLevel() >= candidate.getLevel()) {
                    shadowed = true;
                    break;
                }
            }

            if (!shadowed) {
                kept.add(candidate);
            }
        }

        return kept;
    }
}