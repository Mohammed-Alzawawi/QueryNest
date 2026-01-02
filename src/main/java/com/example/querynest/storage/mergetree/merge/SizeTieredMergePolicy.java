package com.example.querynest.storage.mergetree.merge;

import com.example.querynest.storage.mergetree.MergeTreeConfig;
import com.example.querynest.storage.mergetree.parts.MergeTreePart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SizeTieredMergePolicy implements MergePolicy {

    @Override
    public boolean shouldMerge(List<MergeTreePart> activeParts) {
        if (activeParts == null) return false;
        if (activeParts.size() < MergeTreeConfig.MERGE_THRESHOLD_PARTS) {
            return false;
        }

        Map<Integer, Integer> levelCounts = new HashMap<>();

        for (MergeTreePart part : activeParts) {
            levelCounts.merge(part.getLevel(), 1, Integer::sum);
        }

        for (int count : levelCounts.values()) {
            if (count >= MergeTreeConfig.MERGE_THRESHOLD_PARTS) {
                return true;
            }
        }

        return false;
    }
}