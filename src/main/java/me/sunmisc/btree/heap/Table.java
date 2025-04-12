package me.sunmisc.btree.heap;

import me.sunmisc.btree.index.Index;
import me.sunmisc.btree.index.LongIndex;

import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class Table {
    private static final long HEADER = Long.BYTES;
    private final AtomicLong ids;
    private final File file;

    public Table() {
        this(new File("keys.dat"));
    }

    public Table(final File file) {
        this.file = file;
        this.ids = new AtomicLong(tail()
                .map(Index::offset)
                .orElse(0L) + HEADER
        );
    }

    public Index alloc(final String key, String value) {
        try (final RandomAccessFile rw = new RandomAccessFile(this.file, "rw")) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (DataOutputStream data = new DataOutputStream(buffer)) {
                data.writeUTF(key);
                data.writeUTF(value);
            }
            long offset = ids.getAndAdd(buffer.size());
            rw.seek(offset);
            rw.write(buffer.toByteArray());

            rw.seek(0);
            rw.writeLong(offset);
            return new LongIndex(offset);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String key(final Index index) {
        try (final RandomAccessFile r = new RandomAccessFile(this.file, "r")) {
            r.seek(index.offset());
            return r.readUTF();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Map.Entry<String, String> entry(final Index index) {
        try (final RandomAccessFile r = new RandomAccessFile(this.file, "r")) {
            r.seek(index.offset());
            return Map.entry(r.readUTF(), r.readUTF());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
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
