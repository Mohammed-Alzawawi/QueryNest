package com.example.querynest.storage.mergetree.merge;

import com.example.querynest.storage.mergetree.parts.MergeTreePart;

import java.util.List;

public interface MergePolicy {

    boolean shouldMerge(List<MergeTreePart> activeParts);

}