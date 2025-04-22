package me.sunmisc.btree.heap;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.OptionalLong;

public final class FreelistQueue {
    private final File origin;

    public FreelistQueue(final File origin) {
        this.origin = origin;
    }

    public static void main(String[] args) {
        FreelistQueue queue = new FreelistQueue(new File("nodes"));
        try {
            queue.add(1);
            queue.add(2);
            queue.add(3);

            OptionalLong p = queue.poll();
            System.out.println(p);
            p = queue.poll();
            System.out.println(p);

            p = queue.poll();
            System.out.println(p);

            p = queue.poll();
            System.out.println(p);
            queue.add(5);
            p = queue.poll();
            System.out.println(p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
       //     queue.delete();
        }
    }

    public OptionalLong poll() throws IOException {
        try (final RandomAccessFile raf = new RandomAccessFile(origin, "rw");
             final FileChannel channel = raf.getChannel();
             final FileLock lock = channel.lock()) {

            final long tail = raf.length();
            if (tail == 0) {
                return OptionalLong.empty();
            }
            final long newTail = tail - Long.BYTES;
            raf.seek(newTail);
            final long value = raf.readLong();
            raf.setLength(newTail);
            return OptionalLong.of(value);
        }
    }

    public void add(final long index) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(origin, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.lock()) {
            final long tail = raf.length();
            raf.seek(tail);
            raf.writeLong(index);
        }
    }

    public void clear() throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(origin, "rw")) {
            raf.setLength(0);
        }
    }
}
