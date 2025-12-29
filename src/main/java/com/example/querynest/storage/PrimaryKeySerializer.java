package com.example.querynest.storage;

import java.nio.ByteBuffer;
import java.util.List;

public class PrimaryKeySerializer {

    public static byte[] serializeKeys(List<Integer> keys) {
        ByteBuffer buffer = ByteBuffer.allocate(keys.size() * 4);
        for (int k : keys) buffer.putInt(k);
        return buffer.array();
    }
}
