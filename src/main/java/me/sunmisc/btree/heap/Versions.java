package me.sunmisc.btree.heap;

import me.sunmisc.btree.index.Index;
import me.sunmisc.btree.index.LongIndex;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class Versions {
    private static final long HEADER = Long.BYTES;
    private final AtomicLong ids; // remove
    private final File file;

    public Versions() {
        this(new File("versions.dat"));
    }

    public Versions(File file) {
        this.file = file;
        this.ids = new AtomicLong(tail()
                .map(Index::offset)
                .orElse(HEADER)
        );
    }

    public Index alloc(final Index root) {
        try (final RandomAccessFile file = new RandomAccessFile(this.file, "rw")) {
            final long offset = this.ids.get();
            file.seek(offset);
            file.writeLong(root.offset());
            file.writeLong(System.currentTimeMillis());

            long newEndOfFile = file.getFilePointer();
            ids.set(newEndOfFile);

            file.seek(0);
            file.writeLong(offset);
            return new LongIndex(offset);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map.Entry<Index, Long> find(final Index index) {
        try (final RandomAccessFile file = new RandomAccessFile(this.file, "r")) {
            file.seek(index.offset());
            return Map.entry(new LongIndex(file.readLong()), file.readLong());
        } catch (final IOException e) {
            throw new RuntimeException("index %s".formatted(index.offset()), e);
        }
    }

    public Iterable<Map.Entry<Index, Long>> versions() {
        return tail().map(end -> {
            List<Map.Entry<Index, Long>> list = new LinkedList<>();
            try (final RandomAccessFile file = new RandomAccessFile(this.file, "r")) {
                int c = 0;
                for (long s = Long.BYTES; s < end.offset() + Long.BYTES * 2; s += Long.BYTES * 2) {
                    file.seek(s);
                    list.add(Map.entry(new LongIndex(file.readLong()), file.readLong()));
                    c++;
                    if (c > 100) {
                        list.removeFirst();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return list;
        }).orElse(List.of());
    }
    public int count() {
        return Math.toIntExact(tail().map(e -> e.offset() / (Long.BYTES * 2)).orElse(0L));
    }

    public Optional<Index> tail() {
        try (final RandomAccessFile file = new RandomAccessFile(this.file, "r")) {
            file.seek(0);
            return Optional.of(new LongIndex(file.readLong()));
        } catch (final IOException ex) {
            return Optional.empty();
        }
    }


    public void delete() {
        file.delete();
    }
}
