package me.sunmisc.btree.heap;

import me.sunmisc.btree.index.Index;
import me.sunmisc.btree.index.LongIndex;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

// todo:
public final class Values {
    private static final long HEADER = Long.BYTES * 2;
    private final AtomicLong ids;
    private final File file;

    public Values() {
        this(new File("values.dat"));
    }

    public Values(final File file) {
        AtomicLong ids1;
        this.file = file;
        try (final RandomAccessFile rw = new RandomAccessFile(file, "rw")) {
            rw.seek(0);
            rw.readLong();
            ids1 = new AtomicLong(rw.readLong());
        } catch (IOException e) {
            ids1 = new AtomicLong(HEADER);
        }
        this.ids = ids1;
    }

    public Index alloc(final String key, String value) {
        try (final RandomAccessFile rw = new RandomAccessFile(this.file, "rw")) {

            long offset = ids.get();
            rw.seek(offset);
            rw.writeUTF(key);
            rw.writeUTF(value);

            long newEndOfFile = rw.getFilePointer();
            ids.set(newEndOfFile);

            rw.seek(0);
            rw.writeLong(offset);
            rw.writeLong(newEndOfFile);
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
