package me.sunmisc.btree.heap;

import me.sunmisc.btree.index.Index;
import me.sunmisc.btree.index.LongIndex;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

@Deprecated
public class Table {
    private static final String DATA_FILE = "keys.dat";
    private final AtomicLong ids = new AtomicLong();

    public Index allocate(final String value) {
        final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        final long offset = this.ids.getAndAdd(bytes.length + Integer.BYTES);
        try (final RandomAccessFile file = new RandomAccessFile(DATA_FILE, "rw")) {
            file.seek(offset);
            file.writeInt(bytes.length);
            file.write(bytes);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return new LongIndex(offset);
    }

    public String load(final Index index) {
        try (final RandomAccessFile file = new RandomAccessFile(DATA_FILE, "r")) {
            file.seek(index.offset());

            final int len = file.readInt();
            final byte[] array = new byte[len];
            file.readFully(array);
            return new String(array);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) {
        final Table heap = new Table();
        final Index index1 = heap.allocate("kek");
        final Index index2 = heap.allocate("govno");
        System.out.println(heap.load(index1));
        System.out.println(heap.load(index2));
    }
}
