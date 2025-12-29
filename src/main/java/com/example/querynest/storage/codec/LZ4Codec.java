package com.example.querynest.storage.codec;

import java.io.IOException;

public class LZ4Codec implements CompressionCodec {

    @Override
    public byte[] compress(byte[] data) throws IOException {
        return data;
    }

    @Override
    public byte[] decompress(byte[] data) throws IOException {
        return data;
    }

    @Override
    public String name() {
        return "LZ4";
    }

    @Override
    public byte codecId() {
        return 0x01;
    }
}