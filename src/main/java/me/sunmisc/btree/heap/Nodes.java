package me.sunmisc.btree.heap;

import me.sunmisc.btree.Page;
import me.sunmisc.btree.cow.InternalPage;
import me.sunmisc.btree.cow.LeafPage;
import me.sunmisc.btree.index.Index;
import me.sunmisc.btree.index.LongIndex;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class Nodes {
    private static final long HEADER = Long.BYTES;
    private static final String DATA_FILE = "nodes.dat";
    private final AtomicLong ids;
    private final File file;
    private final Table keys;

    public Nodes(final Table keys) {
        this(new File(DATA_FILE), keys);
    }

    public Nodes(File file, final Table keys) {
        this.file = file;
        this.keys = keys;
        this.ids = new AtomicLong(HEADER); /*new AtomicLong(tail()
                .map(Index::offset)
                .orElse(0L) + HEADER
        );*/
    }

    public Index alloc(final Page page) {
        try (final RandomAccessFile file = new RandomAccessFile(this.file, "rw")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(out);
            try (final InputStream ks = page.keys().bytes();
                 final InputStream cs = page.children().bytes()) {
                final byte[] bks = ks.readAllBytes();
                final byte[] bcs = cs.readAllBytes();
                data.writeInt(bks.length);
                data.write(bks);
                data.writeInt(bcs.length);
                data.write(bcs);
            }

            final long offset = this.ids.getAndAdd(out.size());
            file.seek(offset);
            file.write(out.toByteArray());

            file.seek(0);
            file.writeLong(offset);
            return new LongIndex(offset);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long inc(RandomAccessFile file, long delta) throws IOException {
        try (FileChannel channel = file.getChannel();
             FileLock lock = channel.lock(0, HEADER, false)) {
            try {
                long current = file.readLong();
                file.writeLong(current + delta);
                return current;
            } finally {
                lock.release();
            }
        }
    }

    public Page find(final Index index) {
        try (final RandomAccessFile file = new RandomAccessFile(this.file, "r")) {
            file.seek(index.offset());
            final int keysSize = file.readInt();
            final byte[] keysArray = new byte[keysSize];
            file.read(keysArray);
            final Indexes keys = new ArrayIndexes(keysArray);
            final int childSize = file.readInt();
            if (childSize == 0) {
                return new LeafPage(keys, this.keys, this);
            } else {
                final byte[] childArray = new byte[childSize];
                file.read(childArray);
                final Indexes child = new ArrayIndexes(childArray);
                return new InternalPage(this.keys, this, keys, child);
            }
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
