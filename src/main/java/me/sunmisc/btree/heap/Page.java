package me.sunmisc.btree.heap;

import java.io.*;
import java.util.concurrent.atomic.AtomicLong;

public final class Page {
    private final File file;
    private final long index;
    private final AtomicLong pointer;
    private final int pageSize;

    public Page(File file, long index, int pageSize) {
        this.file = file;
        this.index = index;
        this.pageSize = pageSize;
        this.pointer = new AtomicLong(index);
    }

    public void writeInt(int i) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long pos = pointer.getAndAdd(4);
            raf.seek(pos);
            if (raf.getFilePointer() >= pageSize) {
                throw new IndexOutOfBoundsException();
            }
            raf.writeInt(i);
        }
    }
    public void writeLong(long i) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(index);
            if (raf.getFilePointer() >= pageSize) {
                throw new IndexOutOfBoundsException();
            }
            raf.writeLong(i);
        }
    }

    public int readInt() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(index);
            if (raf.getFilePointer() >= pageSize) {
                throw new IndexOutOfBoundsException();
            }
            return raf.readInt();
        }
    }
    public long size() {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(index);
            return raf.getFilePointer() - index;
        } catch (IOException e) {
            return 0;
        }
    }
}
