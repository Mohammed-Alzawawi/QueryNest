package com.example.querynest.storage.mergetree;

public final class MergeTreeConfig {

    public static final int GRANULE_SIZE = 8192;
    public static final int UNCOMPRESSED_BLOCK_SIZE = 64 * 1024;

    public static final boolean COMPRESSED_ENABLED = false;

    public static final int MERGE_THRESHOLD_PARTS = 5;
    public static final long MERGE_INTERVAL_MS = 1_000;

    private MergeTreeConfig() {
    }
}
