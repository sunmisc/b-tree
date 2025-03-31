package me.sunmisc.btree.heap;

import me.sunmisc.btree.Page;
import me.sunmisc.btree.cow.InternalPage;
import me.sunmisc.btree.cow.LeafPage;
import me.sunmisc.btree.index.Index;
import me.sunmisc.btree.index.LongIndex;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

public class Nodes {
    private static final int PAGE_SIZE = 512;
    private static final String DATA_FILE = "nodes.dat";
    private final AtomicLong ids = new AtomicLong();
    private final AtomicLong deleted = new AtomicLong();
    private final Table table;

    public Nodes(final Table table) {
        this.table = table;
    }

    public Index allocate(final Page page) {
        final long offset = this.ids.getAndAdd(PAGE_SIZE);
        final Indexes keys = page.keys();
        final Indexes child = page.children();
        try (final RandomAccessFile file = new RandomAccessFile(DATA_FILE, "rw");
             final FileChannel channel = file.getChannel().position(offset)) {
            final ByteBuffer buffer = ByteBuffer.allocate(PAGE_SIZE);
            try (final InputStream ks = keys.bytes()) {
                buffer.putInt(keys.size());
                buffer.put(ks.readAllBytes());
            }
            try (final InputStream cs = child.bytes()) {
                buffer.putInt(child.size());
                buffer.put(cs.readAllBytes());
            }
            buffer.flip();
            channel.write(buffer);
            return new LongIndex(offset);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Page load(final Index index) {
        try (final RandomAccessFile file = new RandomAccessFile(DATA_FILE, "rw")) {
            final MappedByteBuffer buffer = file.getChannel()
                    .map(FileChannel.MapMode.READ_ONLY, index.offset(), PAGE_SIZE);
            final int keysSize = buffer.getInt();
            final byte[] keysArray = new byte[keysSize * Long.BYTES];
            buffer.get(keysArray);
            final Indexes keys = new ArrayIndexes(keysArray);
            final int childSize = buffer.getInt();
            if (childSize == 0) {
                return new LeafPage(keys, this.table, this);
            } else {
                final byte[] childArray = new byte[childSize * Long.BYTES];
                buffer.get(childArray);
                final Indexes child = new ArrayIndexes(childArray);
                return new InternalPage(this.table, this, keys, child);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
