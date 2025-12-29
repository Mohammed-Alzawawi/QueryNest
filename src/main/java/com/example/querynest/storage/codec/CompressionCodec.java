package com.example.querynest.storage.codec;

import java.io.IOException;

public interface CompressionCodec {
    byte[] compress(byte[] data) throws IOException;
    byte[] decompress(byte[] data) throws IOException;
    String name();
    byte codecId();
}