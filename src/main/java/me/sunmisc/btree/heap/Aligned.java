package me.sunmisc.btree.heap;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Aligned {
    private final File file;
    private final int pageSize;
    private final long offset;

    public Aligned(File file, int pageSize, long headers) {
        this.file = file;
        this.pageSize = pageSize;
        this.offset = headers;
    }

    public static void main(String[] args) throws IOException {
        Aligned aligned = new Aligned(new File("kek.dat"), 4, 1);
        try {
            List<Long> indices = aligned.alloc(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5}));
            System.out.println("last "+(aligned.last()));
            System.out.println("size "+aligned.size());
            System.out.println(indices);

            indices.forEach(idx -> {
                try {
                    System.out.println(Arrays.toString(aligned.fetch(idx).readAllBytes()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } finally {
            aligned.delete();
        }
    }

    public List<Long> alloc(InputStream input) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long offset = last();
            List<Long> pages = alloc0(raf, offset, input);
            raf.seek(0);
            raf.writeLong(offset + ((long) pages.size() * pageSize));
            return pages;
        }
    }

    private List<Long> alloc0(RandomAccessFile raf, long pos, InputStream input) throws IOException {
        byte[] bytes = input.readAllBytes();
        int pages = Math.ceilDiv(bytes.length, pageSize);
        List<Long> accumulate = new ArrayList<>(pages);
        long offset = pos;
        for (int i = 0; i < pages; ++i) {
            int start = i * pageSize;
            write(raf, offset, new ByteArrayInputStream(
                    bytes,
                    start,
                    Math.min(bytes.length, pageSize))
            );
            accumulate.add(offset);
            offset += pageSize;
        }
        return accumulate;
    }

    private void write(RandomAccessFile raf, long pos, InputStream stream) throws IOException {
        final byte[] bytes = new byte[pageSize];
        stream.read(bytes);
        raf.seek(pos);
        raf.write(bytes);
    }

    public InputStream fetch(long offset) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(offset);
            byte[] bytes = new byte[pageSize];
            raf.read(bytes);
            return new ByteArrayInputStream(bytes);
        }
    }

    public long last() {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            return raf.readLong();
        } catch (IOException ex) {
            return 8 + offset;
        }
    }

    public int size() {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            return Math.toIntExact((raf.readLong() - (8 + offset)) / pageSize);
        } catch (IOException ex) {
            return 0;
        }
    }
    public void delete() {
        file.delete();
    }
}