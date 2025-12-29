package com.example.querynest.storage;

public class StorageConfig {
    private final int indexGranularity;
    private final long indexGranularityBytes;
    private final long maxBlockSize;
    private final long memoryThreshold;
    private final boolean adaptiveGranularity;

    public StorageConfig(int indexGranularity,
                         long indexGranularityBytes,
                         long maxBlockSize,
                         long memoryThreshold,
                         boolean adaptiveGranularity) {
        this.indexGranularity = indexGranularity;
        this.indexGranularityBytes = indexGranularityBytes;
        this.maxBlockSize = maxBlockSize;
        this.memoryThreshold = memoryThreshold;
        this.adaptiveGranularity = adaptiveGranularity;
    }

    public static StorageConfig defaults() {
        return new StorageConfig(8192, 10 * 1024 * 1024,
                1024 * 1024, 100 * 1024 * 1024, true);
    }

    public int getIndexGranularity() { return indexGranularity; }
    public long getIndexGranularityBytes() { return indexGranularityBytes; }
    public long getMaxBlockSize() { return maxBlockSize; }
    public long getMemoryThreshold() { return memoryThreshold; }
    public boolean isAdaptiveGranularity() { return adaptiveGranularity; }
}
