package com.example.querynest.storage.codec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DeltaCodec implements CompressionCodec {

    @Override
    public byte[] compress(byte[] data) throws IOException {
        if (data.length < 8) {
            return data;
        }

        int numValues = data.length / 8;
        byte[] result = new byte[data.length];
        ByteBuffer input = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer output = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);

        long previous = 0;
        for (int i = 0; i < numValues; i++) {
            long current = input.getLong();
            long delta = current - previous;
            output.putLong(delta);
            previous = current;
        }

        return result;
    }

    @Override
    public byte[] decompress(byte[] data) throws IOException {
        if (data.length < 8) {
            return data;
        }

        int numValues = data.length / 8;
        byte[] result = new byte[data.length];
        ByteBuffer input = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer output = ByteBuffer.wrap(result).order(ByteOrder.LITTLE_ENDIAN);

        long previous = 0;
        for (int i = 0; i < numValues; i++) {
            long delta = input.getLong();
            long current = previous + delta;
            output.putLong(current);
            previous = current;
        }

        return result;
    }

    @Override
    public String name() {
        return "Delta";
    }

    @Override
    public byte codecId() {
        return 0x03;
    }
}