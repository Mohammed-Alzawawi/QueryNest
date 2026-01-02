package com.example.querynest.storage.mergetree.parts;

import java.util.Comparator;

public final class PartOrdering {

    private PartOrdering() {
    }

    public static final Comparator<MergeTreePart> BY_RANGE_AND_LEVEL =
            Comparator.comparingLong(MergeTreePart::getMinBlock)
                    .thenComparingLong(MergeTreePart::getMaxBlock)
                    .thenComparing(
                            Comparator.comparingInt(MergeTreePart::getLevel).reversed()
                    )
                    .thenComparing(MergeTreePart::getName);

}