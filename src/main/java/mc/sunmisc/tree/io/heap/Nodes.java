package mc.sunmisc.tree.io.heap;

import mc.sunmisc.tree.io.InternalPage;
import mc.sunmisc.tree.io.LeafPage;
import mc.sunmisc.tree.io.Page;
import mc.sunmisc.tree.io.index.Index;
import mc.sunmisc.tree.io.index.LongIndex;

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

    public Nodes(Table table) {
        this.table = table;
    }

    public Index allocate(final Page page) {
        final long offset = ids.getAndAdd(PAGE_SIZE);
        final Indexes keys = page.keys();
        final Indexes child = page.children();
        try (RandomAccessFile file = new RandomAccessFile(DATA_FILE, "rw");
             FileChannel channel = file.getChannel()) {
            channel.position(offset);
            ByteBuffer byteBuffer = ByteBuffer.allocate(PAGE_SIZE);

            try (InputStream ks = keys.bytes()) {
                byteBuffer.putInt(keys.size());
                byteBuffer.put(ks.readAllBytes());
            }
            try (InputStream cs = child.bytes()) {
                byteBuffer.putInt(child.size());
                byteBuffer.put(cs.readAllBytes());
            }
            byteBuffer.flip();
            channel.write(byteBuffer);
            return new LongIndex(offset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Page load(final Index index) {
        try (RandomAccessFile file = new RandomAccessFile(DATA_FILE, "r")) {
            MappedByteBuffer buffer = file.getChannel()
                    .map(FileChannel.MapMode.READ_ONLY, index.offset(), PAGE_SIZE);
            final int keysSize = buffer.getInt();
            final byte[] keysArray = new byte[keysSize * Long.BYTES];
            buffer.get(keysArray);
            final Indexes keys = new ArrayIndexes(keysArray);
            final int childSize = buffer.getInt();
            if (childSize == 0) {
                return new LeafPage(keys, table, this);
            } else {
                final byte[] childArray = new byte[childSize * Long.BYTES];
                buffer.get(childArray);
                final Indexes child = new ArrayIndexes(childArray);
                return new InternalPage(table, this, keys, child);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
