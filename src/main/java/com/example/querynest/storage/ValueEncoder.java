package com.example.querynest.storage;

import java.nio.ByteBuffer;

public class ValueEncoder {

    public static byte[] encodeInt(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static byte[] encodeLong(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    public static byte[] encodeString(String value) {
        return value.getBytes();
    }
}
