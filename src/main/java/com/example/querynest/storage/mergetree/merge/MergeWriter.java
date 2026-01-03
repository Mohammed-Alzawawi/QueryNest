package com.example.querynest.storage.mergetree.merge;

import com.example.querynest.ast.ColumnDefinition;
import com.example.querynest.storage.mergetree.parts.MergeTreePart;
import com.example.querynest.storage.mergetree.parts.PartNameGenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;

public final class MergeWriter {

    private final Path tableDir;
    private final List<ColumnDefinition> columns;

    public MergeWriter(Path tableDir, List<ColumnDefinition> columns) {
        this.tableDir = Objects.requireNonNull(tableDir);
        this.columns = Objects.requireNonNull(columns);
    }

    public MergeTreePart merge(List<MergeTreePart> sourceParts) throws IOException {
        if (sourceParts == null || sourceParts.isEmpty()) {
            throw new IllegalArgumentException("sourceParts is empty");
        }

        List<MergeTreePart> sources = new ArrayList<>(sourceParts);
        sources.sort(Comparator.comparingLong(MergeTreePart::getMinBlock)
                .thenComparingLong(MergeTreePart::getMaxBlock)
                .thenComparingInt(MergeTreePart::getLevel));

        long minBlock = sources.stream().mapToLong(MergeTreePart::getMinBlock).min().orElseThrow();
        long maxBlock = sources.stream().mapToLong(MergeTreePart::getMaxBlock).max().orElseThrow();
        int newLevel = sources.stream().mapToInt(MergeTreePart::getLevel).max().orElse(0) + 1;

        PartNameGenerator gen = new PartNameGenerator(maxBlock);
        String finalName = gen.mergedPartName(minBlock, maxBlock, newLevel);
        String tmpName = gen.toTempName(finalName);

        Path partsDir = tableDir.resolve("parts");
        Path tmpRoot = tableDir.resolve("tmp");
        Files.createDirectories(partsDir);
        Files.createDirectories(tmpRoot);

        Path tmpDir = tmpRoot.resolve(tmpName);
        if (Files.exists(tmpDir)) {
            deleteRecursive(tmpDir);
        }
        Files.createDirectories(tmpDir);

        long totalRows = 0;
        for (MergeTreePart p : sources) {
            totalRows += readCount(p.getPath());
        }

        for (ColumnDefinition col : columns) {
            String colName = col.name();
            Path outBin = tmpDir.resolve(colName + ".bin");
            Path outMrk = tmpDir.resolve(colName + ".mrk3");

            long baseBinOffset = 0;
            try (FileOutputStream binOut = new FileOutputStream(outBin.toFile());
                 FileOutputStream mrkOut = new FileOutputStream(outMrk.toFile())) {

                for (MergeTreePart part : sources) {
                    Path partDir = part.getPath();

                    Path inBin = partDir.resolve(colName + ".bin");
                    Path inMrk = partDir.resolve(colName + ".mrk3");

                    long inBinSize = Files.size(inBin);

                    try (InputStream in = Files.newInputStream(inBin)) {
                        in.transferTo(binOut);
                    }

                    List<Mark> marks = readMarks(inMrk);
                    for (Mark m : marks) {
                        writeMark(mrkOut,
                                new Mark(baseBinOffset + m.compressedOffset,
                                        m.uncompressedOffset));
                    }

                    baseBinOffset += inBinSize;
                }

                binOut.flush();
                binOut.getFD().sync();
                mrkOut.flush();
                mrkOut.getFD().sync();
            }
        }

        Path outPk = tmpDir.resolve("primary.idx");
        try (FileOutputStream pkOut = new FileOutputStream(outPk.toFile())) {
            for (MergeTreePart part : sources) {
                Path pk = part.getPath().resolve("primary.idx");
                if (Files.exists(pk)) {
                    try (InputStream in = Files.newInputStream(pk)) {
                        in.transferTo(pkOut);
                    }
                }
            }
            pkOut.flush();
            pkOut.getFD().sync();
        }

        writeColumnsTxt(tmpDir);
        writeDefaultCompressionCodec(tmpDir);
        Files.writeString(tmpDir.resolve("count.txt"), String.valueOf(totalRows));
        writeChecksums(tmpDir);

        Path finalDir = partsDir.resolve(finalName);
        if (Files.exists(finalDir)) {
            throw new IOException("Merged part already exists: " + finalDir);
        }
        Files.move(tmpDir, finalDir, StandardCopyOption.ATOMIC_MOVE);

        return new MergeTreePart(finalName, finalDir, minBlock, maxBlock, newLevel, totalRows);
    }

    private long readCount(Path partDir) throws IOException {
        Path count = partDir.resolve("count.txt");
        if (!Files.isRegularFile(count)) return 0L;
        String s = Files.readString(count).trim();
        if (s.isEmpty()) return 0L;
        try {
            return Math.max(0L, Long.parseLong(s));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private void writeColumnsTxt(Path dir) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (ColumnDefinition col : columns) {
            sb.append(col.name())
                    .append("\t")
                    .append(col.dataType())
                    .append("\n");
        }
        Files.writeString(dir.resolve("columns.txt"), sb.toString());
    }

    private void writeDefaultCompressionCodec(Path dir) throws IOException {
        Files.writeString(dir.resolve("default_compression_codec.txt"), "LZ4");
    }

    private void writeChecksums(Path dir) throws IOException {
        Path file = dir.resolve("checksums.txt");
        StringBuilder sb = new StringBuilder();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                if (!Files.isRegularFile(p)) continue;

                byte[] data = Files.readAllBytes(p);
                CRC32 crc = new CRC32();
                crc.update(data);

                sb.append(p.getFileName())
                        .append("\t")
                        .append(data.length)
                        .append("\t")
                        .append(Long.toHexString(crc.getValue()))
                        .append("\n");
            }
        }

        Files.writeString(file, sb.toString());
    }

    private static final class Mark {
        final long compressedOffset;
        final long uncompressedOffset;

        Mark(long compressedOffset, long uncompressedOffset) {
            this.compressedOffset = compressedOffset;
            this.uncompressedOffset = uncompressedOffset;
        }
    }

    private List<Mark> readMarks(Path mrk3) throws IOException {
        byte[] bytes = Files.readAllBytes(mrk3);
        if (bytes.length == 0) return List.of();
        if (bytes.length % 16 != 0) {
            throw new IOException("Invalid mrk3 length: " + mrk3);
        }

        List<Mark> marks = new ArrayList<>(bytes.length / 16);
        ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        while (buf.remaining() >= 16) {
            marks.add(new Mark(buf.getLong(), buf.getLong()));
        }
        return marks;
    }

    private void writeMark(FileOutputStream out, Mark mark) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        buf.putLong(mark.compressedOffset);
        buf.putLong(mark.uncompressedOffset);
        out.write(buf.array());
    }

    private void deleteRecursive(Path root) throws IOException {
        if (root == null || !Files.exists(root)) return;
        Files.walk(root)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
    }
}