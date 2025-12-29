package com.example.querynest.storage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MarkEntry {
    private final long compressedOffset;
    private final long uncompressedOffset;

    public MarkEntry(long compressedOffset, long uncompressedOffset) {
        this.compressedOffset = compressedOffset;
        this.uncompressedOffset = uncompressedOffset;
    }

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putLong(compressedOffset);
        buffer.putLong(uncompressedOffset);
        return buffer.array();
    }

    public long getCompressedOffset() {
        return compressedOffset;
    }

    public long getUncompressedOffset() {
        return uncompressedOffset;
    }
}