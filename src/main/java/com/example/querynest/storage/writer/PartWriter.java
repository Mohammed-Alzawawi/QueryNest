package com.example.querynest.storage.writer;

import com.example.querynest.ast.ColumnDefinition;
import com.example.querynest.ast.constraints.Constraint;
import com.example.querynest.storage.*;
import com.example.querynest.storage.codec.CodecFactory;
import com.example.querynest.storage.codec.CompressionCodec;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;


public class PartWriter {

    private static final AtomicLong PART_COUNTER = new AtomicLong(0);

    private final String tableName;
    private final List<ColumnDefinition> columns;
    private final List<Constraint> constraints;
    private final String databasePath;
    private final StorageConfig config;

    private Path tempPartDir;
    private Map<String, ColumnState<?>> columnStates;
    private List<byte[]> primaryIndexEntries;
    private long rowIndex;
    private long nextGranuleRow;

    public PartWriter(String tableName,
                      List<ColumnDefinition> columns,
                      List<Constraint> constraints,
                      String databasePath,
                      StorageConfig config) {
        this.tableName = tableName;
        this.columns = columns;
        this.constraints = constraints;
        this.databasePath = databasePath;
        this.config = config;
    }

    public void writeRows(List<Map<String, Object>> rows) throws IOException {
        initialize();

        for (Map<String, Object> row : rows) {
            writeRow(row);
        }

        finalize1();
    }

    private void initialize() throws IOException {
        long partId = PART_COUNTER.incrementAndGet();
        String partName = String.format("part_%d_%d_%s",
                System.currentTimeMillis(),
                partId,
                UUID.randomUUID().toString().substring(0, 8));

        Path dbPath = Paths.get(databasePath);
        Path tempDir = dbPath.resolve("tmp");
        Files.createDirectories(tempDir);

        tempPartDir = tempDir.resolve(partName);
        Files.createDirectories(tempPartDir);

        columnStates = new LinkedHashMap<>();
        primaryIndexEntries = new ArrayList<>();

        for (ColumnDefinition col : columns) {
            String colFileName = col.getName() + ".bin"; // No col_ prefix
            Path colFile = tempPartDir.resolve(colFileName);
            FileOutputStream fos = new FileOutputStream(colFile.toFile());

            CompressionCodec codec = CodecFactory.getCodecForType(col.getDataType());

            ColumnState<?> state = new ColumnState<>(
                    col.getName(),
                    col.getDataType(),
                    col.isNullable(),
                    codec,
                    fos
            );

            columnStates.put(col.getName(), state);
        }

        rowIndex = 0;
        nextGranuleRow = 0;
    }

    private void writeRow(Map<String, Object> row) throws IOException {
        if (rowIndex == nextGranuleRow) {
            handleGranuleBoundary(row);
        }

        for (ColumnDefinition col : columns) {
            Object value = row.get(col.getName());
            ColumnState<?> state = columnStates.get(col.getName());

            @SuppressWarnings("unchecked")
            ColumnState<Object> typedState = (ColumnState<Object>) state;
            typedState.writeValue(value);
        }

        if (rowIndex > 0 && rowIndex % 1024 == 0) {
            for (ColumnState<?> state : columnStates.values()) {
                state.flushBlock();
            }
        }

        rowIndex++;
    }

    private void handleGranuleBoundary(Map<String, Object> row) {
        byte[] pkValue = buildPrimaryKey(row);
        primaryIndexEntries.add(pkValue);

        for (ColumnState<?> state : columnStates.values()) {
            state.addMark();
        }

        nextGranuleRow += config.getIndexGranularity();
    }

    private byte[] buildPrimaryKey(Map<String, Object> row) {
        List<String> pkColumns = PrimaryKeyBuilder.extractPrimaryKeyColumns(
                columns, constraints);

        if (pkColumns.isEmpty()) {
            return new byte[0];
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (String pkCol : pkColumns) {
            Object value = row.get(pkCol);
            if (value != null) {
                byte[] encoded = String.valueOf(value).getBytes();
                baos.write(encoded, 0, encoded.length);
            }
        }
        return baos.toByteArray();
    }

    private void finalize1() throws IOException {
        for (ColumnState<?> state : columnStates.values()) {
            state.close();
        }

        writeMarkFiles();
        writePrimaryIndex();
        writeColumnsTxt();
        writeDefaultCompressionCodec();
        writeChecksums();

        atomicPublish();
    }

    private void writeMarkFiles() throws IOException {
        for (ColumnDefinition col : columns) {
            ColumnState<?> state = columnStates.get(col.getName());
            String markFileName = col.getName() + ".mrk3"; // No col_ prefix
            Path markFile = tempPartDir.resolve(markFileName);

            try (FileOutputStream fos = new FileOutputStream(markFile.toFile())) {
                for (MarkEntry mark : state.getMarkEntries()) {
                    fos.write(mark.toBytes());
                }
                fos.flush();
                fos.getFD().sync();
            }
        }
    }

    private void writePrimaryIndex() throws IOException {
        Path primaryIdxFile = tempPartDir.resolve("primary.idx");

        try (FileOutputStream fos = new FileOutputStream(primaryIdxFile.toFile())) {
            for (byte[] pkEntry : primaryIndexEntries) {
                fos.write(pkEntry);
            }
            fos.flush();
            fos.getFD().sync();
        }
    }

    private void writeColumnsTxt() throws IOException {
        Path file = tempPartDir.resolve("columns.txt");

        StringBuilder sb = new StringBuilder();
        for (ColumnDefinition col : columns) {
            sb.append(col.getName())
                    .append("\t")
                    .append(col.getDataType())
                    .append("\n");
        }

        Files.writeString(file, sb.toString());
    }

    private void writeDefaultCompressionCodec() throws IOException {
        Path file = tempPartDir.resolve("default_compression_codec.txt");
        Files.writeString(file, "LZ4"); // clickHouse default algo
    }


    private void writeChecksums() throws IOException {
        Path file = tempPartDir.resolve("checksums.txt");
        StringBuilder sb = new StringBuilder();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempPartDir)) {
            for (Path p : stream) {
                if (Files.isRegularFile(p)) {
                    byte[] data = Files.readAllBytes(p);
                    CRC32 crc = new CRC32();
                    crc.update(data);

                    long size = data.length;
                    long checksum = crc.getValue();

                    sb.append(p.getFileName())
                            .append("\t")
                            .append(size)
                            .append("\t")
                            .append(Long.toHexString(checksum))
                            .append("\n");
                }
            }
        }

        Files.writeString(file, sb.toString());
    }

    private void atomicPublish() throws IOException {
        Path dbPath = Paths.get(databasePath);
        Path tableDir = dbPath.resolve(tableName);
        Files.createDirectories(tableDir);

        Path finalPartDir = tableDir.resolve(tempPartDir.getFileName());

        if (Files.exists(finalPartDir)) {
            throw new IOException("Part already exists: " + finalPartDir);
        }

        Files.move(tempPartDir, finalPartDir, StandardCopyOption.ATOMIC_MOVE);
    }
}
