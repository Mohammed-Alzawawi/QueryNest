package com.example.querynest.storage;

import com.example.querynest.storage.codec.CompressionCodec;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class ColumnState<T> {
    private final String columnName;
    private final String dataType;
    private final boolean nullable;
    private final CompressionCodec codec;
    private final FileOutputStream binFile;
    private final List<MarkEntry> markEntries;
    private final BitSet nullBitmap;

    private final ByteArrayOutputStream buffer;
    private long compressedOffset;
    private int rowsInBuffer;
    private int nullCount;

    public ColumnState(String columnName, String dataType, boolean nullable,
                       CompressionCodec codec, FileOutputStream binFile) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.nullable = nullable;
        this.codec = codec;
        this.binFile = binFile;
        this.markEntries = new ArrayList<>();
        this.nullBitmap = nullable ? new BitSet() : null;
        this.buffer = new ByteArrayOutputStream();
        this.compressedOffset = 0;
        this.rowsInBuffer = 0;
        this.nullCount = 0;
    }

    public void writeValue(T value) throws IOException {
        if (nullable) {
            if (value == null) {
                nullBitmap.set(rowsInBuffer);
                nullCount++;
            }
        }

        if (value != null) {
            byte[] encoded = encodeValue(value);
            buffer.write(encoded);
        }

        rowsInBuffer++;
    }

    public void addMark() {
        long uncompressedOffset = buffer.size();
        markEntries.add(new MarkEntry(compressedOffset, uncompressedOffset));
    }

    public void flushBlock() throws IOException {
        if (buffer.size() == 0 && nullCount == 0) {
            return;
        }

        byte[] uncompressedData = buffer.toByteArray();
        byte[] dataToWrite;

        if (uncompressedData.length > 100) {
            try {
                byte[] compressed = codec.compress(uncompressedData);
                if (compressed.length < uncompressedData.length * 0.9) {
                    dataToWrite = compressed;
                } else {
                    dataToWrite = uncompressedData;
                }
            } catch (Exception e) {
                dataToWrite = uncompressedData;
            }
        } else {
            dataToWrite = uncompressedData;
        }

        ByteBuffer header = ByteBuffer.allocate(9);
        header.order(ByteOrder.LITTLE_ENDIAN);
        header.put(codec.codecId());
        header.putInt(uncompressedData.length);
        header.putInt(dataToWrite.length);

        binFile.write(header.array());
        binFile.write(dataToWrite);

        compressedOffset += header.array().length + dataToWrite.length;

        buffer.reset();
        rowsInBuffer = 0;
    }

    public void close() throws IOException {
        flushBlock();

        if (nullable && nullBitmap.length() > 0) {
            writeNullBitmap();
        }

        binFile.flush();
        binFile.getFD().sync();
        binFile.close();
    }

    private void writeNullBitmap() throws IOException {
        byte[] bitmapBytes = nullBitmap.toByteArray();
        ByteBuffer header = ByteBuffer.allocate(8);
        header.order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(nullCount);
        header.putInt(bitmapBytes.length);

        binFile.write(header.array());
        binFile.write(bitmapBytes);
    }

    private byte[] encodeValue(T value) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        switch (dataType.toUpperCase()) {
            case "INTEGER", "INT" -> {
                if (value instanceof Number) {
                    buffer.putInt(((Number) value).intValue());
                }
            }
            case "BIGINT", "LONG" -> {
                if (value instanceof Number) {
                    buffer.putLong(((Number) value).longValue());
                }
            }
            case "DOUBLE", "DECIMAL" -> {
                if (value instanceof Number) {
                    buffer.putDouble(((Number) value).doubleValue());
                }
            }
            case "BOOLEAN" -> {
                if (value instanceof Boolean) {
                    buffer.put((byte) ((Boolean) value ? 1 : 0));
                }
            }
            case "VARCHAR", "TEXT", "STRING" -> {
                byte[] strBytes = value.toString().getBytes();
                buffer.putInt(strBytes.length);
                buffer.put(strBytes);
            }
            default -> {
                byte[] strBytes = value.toString().getBytes();
                buffer.putInt(strBytes.length);
                buffer.put(strBytes);
            }
        }

        byte[] result = new byte[buffer.position()];
        buffer.flip();
        buffer.get(result);
        return result;
    }

    public List<MarkEntry> getMarkEntries() {
        return new ArrayList<>(markEntries);
    }

    public String getCodecName() {
        return codec.name();
    }

    public int getNullCount() {
        return nullCount;
    }
}
