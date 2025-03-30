package mc.sunmisc.tree.io.heap;

import mc.sunmisc.tree.io.index.Index;
import mc.sunmisc.tree.io.index.LongIndex;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

public class Table {
    private static final String DATA_FILE = "keys.dat";
    private final AtomicLong ids = new AtomicLong();

    public Index allocate(final String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        final long offset = ids.getAndAdd(bytes.length + Integer.BYTES);
        try (RandomAccessFile file = new RandomAccessFile(DATA_FILE, "rw")) {
            file.seek(offset);
            file.writeInt(bytes.length);
            file.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new LongIndex(offset);
    }

    public String load(final Index index) {
        try (RandomAccessFile file = new RandomAccessFile(DATA_FILE, "r")) {
            file.seek(index.offset());

            int len = file.readInt();
            byte[] array = new byte[len];
            file.readFully(array);
            return new String(array);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Table heap = new Table();
        Index index1 = heap.allocate("kek");
        Index index2 = heap.allocate("govno");
        System.out.println(heap.load(index1));
        System.out.println(heap.load(index2));
    }
}
