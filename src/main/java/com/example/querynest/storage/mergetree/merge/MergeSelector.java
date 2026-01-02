package com.example.querynest.storage.mergetree.merge;

import com.example.querynest.storage.mergetree.parts.MergeTreePart;

import java.util.*;

public final class MergeSelector {

    public List<MergeTreePart> select(List<MergeTreePart> activeParts) {
        if (activeParts == null || activeParts.isEmpty()) return List.of();

        Map<Integer, List<MergeTreePart>> byLevel = new HashMap<>();
        for (MergeTreePart part : activeParts) {
            byLevel.computeIfAbsent(part.getLevel(), k -> new ArrayList<>()).add(part);
        }

        for (List<MergeTreePart> sameLevel : byLevel.values()) {
            if (sameLevel.size() < 2) continue;

            sameLevel.sort(Comparator.comparingLong(MergeTreePart::getMinBlock));

            for (int i = 0; i < sameLevel.size() - 1; i++) {
                MergeTreePart a = sameLevel.get(i);
                MergeTreePart b = sameLevel.get(i + 1);

                if (a.getMaxBlock() + 1 == b.getMinBlock()) {
                    return List.of(a, b);
                }
            }
        }

        return List.of();
    }
}