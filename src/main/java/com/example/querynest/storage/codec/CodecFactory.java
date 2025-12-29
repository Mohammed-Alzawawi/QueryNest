package com.example.querynest.storage.codec;

public class CodecFactory {

    public static CompressionCodec getCodecForType(String dataType) {
        return switch (dataType.toUpperCase()) {
            case "INTEGER", "BIGINT", "LONG" -> new DeltaCodec();
            case "VARCHAR", "TEXT", "STRING" -> new LZ4Codec();
            default -> new LZ4Codec();
        };
    }

    public static CompressionCodec getCodec(String name) {
        return switch (name.toUpperCase()) {
            case "LZ4" -> new LZ4Codec();
            case "DELTA" -> new DeltaCodec();
            case "NONE" -> new NoneCodec();
            default -> new LZ4Codec();
        };
    }

    static class NoneCodec implements CompressionCodec {
        public byte[] compress(byte[] data) {
            return data;
        }

        public byte[] decompress(byte[] data) {
            return data;
        }

        public String name() {
            return "None";
        }

        public byte codecId() {
            return 0x00;
        }
    }
}