package me.sunmisc.btree.heap;

import me.sunmisc.btree.Page;
import me.sunmisc.btree.cow.InternalPage;
import me.sunmisc.btree.cow.LeafPage;
import me.sunmisc.btree.index.Index;
import me.sunmisc.btree.index.LongIndex;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class Nodes {
    private static final long HEADER = Long.BYTES * 2;
    private final AtomicLong ids; // remove
    private final File file;
    private final Table table;

    public Nodes(final Table table) {
        this(new File("nodes.dat"), table);
    }

    public Nodes(File file, Table table) {
        this.file = file;
        this.table = table;
        AtomicLong ids1;
        try (final RandomAccessFile rw = new RandomAccessFile(file, "rw")) {
            rw.seek(0);
            rw.readLong();
            ids1 = new AtomicLong(rw.readLong());
        } catch (IOException e) {
            ids1 = new AtomicLong(HEADER);
        }
        this.ids = ids1;
    }

    public Index alloc(final Page page) {
        try (final RandomAccessFile file = new RandomAccessFile(this.file, "rw");) {
            final long offset = this.ids.get();
            file.seek(offset);
            byte[] ks = page.keys().bytes().readAllBytes();
            byte[] chs = page.children().bytes().readAllBytes();
            file.writeInt(ks.length);
            file.write(ks);

            file.writeInt(chs.length);
            file.write(chs);

            long newEndOfFile = file.getFilePointer();
            ids.set(newEndOfFile);

            file.seek(0);
            file.writeLong(offset);
            file.writeLong(newEndOfFile);
            return new LongIndex(offset);
        } catch (final IOException e) {
            throw new RuntimeException(e);
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
                return new LeafPage(table, keys);
            } else {
                final byte[] childArray = new byte[childSize];
                file.read(childArray);
                final Indexes child = new ArrayIndexes(childArray);
                return new InternalPage(table, keys, child);
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
