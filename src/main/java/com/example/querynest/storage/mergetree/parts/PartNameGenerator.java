package com.example.querynest.storage.mergetree.parts;

import com.example.querynest.storage.mergetree.MergeTreeConfig;

import java.util.concurrent.atomic.AtomicLong;

public final class PartNameGenerator {

    private final AtomicLong blockCounter;

    public PartNameGenerator(long initialBlock) {
        this.blockCounter = new AtomicLong(initialBlock);
    }

    public String nextInsertPartName(long rows) {
        long start = blockCounter.get() + 1;
        long blocks = Math.max(1L, rows / MergeTreeConfig.GRANULE_SIZE);
        long end = blockCounter.addAndGet(blocks);
        return start + "_" + end + "_0";
    }

    public String mergedPartName(long minBlock, long maxBlock, int level) {
        return minBlock + "_" + maxBlock + "_" + level;
    }

    public String toTempName(String finalName) {
        return finalName + "_tmp";
    }

    public long currentBlock() {
        return blockCounter.get();
    }
}