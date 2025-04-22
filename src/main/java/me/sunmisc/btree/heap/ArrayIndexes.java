package me.sunmisc.btree.heap;

import me.sunmisc.btree.index.Index;
import me.sunmisc.btree.index.LongIndex;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public final class ArrayIndexes implements Indexes {
    private static final byte[] EMPTY = new byte[0];
    private static final VarHandle LONG = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
    private final byte[] bytes;

    public ArrayIndexes() {
        this(EMPTY);
    }

    public ArrayIndexes(final byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public int size() {
        return Math.ceilDiv(this.bytes.length, Long.BYTES);
    }

    @Override
    public ArrayIndexes set(final int pos, final Index value) {
        final int idx = pos * Long.BYTES;
        final byte[] array = this.bytes.clone();
        LONG.set(array, idx, value.offset());
        return new ArrayIndexes(array);
    }

    @Override
    public ArrayIndexes add(final int pos, final Index... values) {
        final int space = values.length * Long.BYTES;
        final int idx = pos * Long.BYTES;
        final int len = this.bytes.length;
        final int numMoved = len - idx;
        final byte[] newElements;
        if (numMoved == 0) {
            newElements = Arrays.copyOf(this.bytes, len + space);
        } else {
            newElements = new byte[len + space];
            System.arraycopy(this.bytes, 0, newElements, 0, idx);
            System.arraycopy(this.bytes, idx,
                    newElements, idx + space,
                    numMoved);
        }
        for (int i = 0; i < values.length; ++i) {
            LONG.set(newElements, idx + (i * Long.BYTES), values[i].offset());
        }
        return new ArrayIndexes(newElements);
    }

    @Override
    public Indexes addAll(Indexes index) {
        Indexes indexes = this;
        for (Index ix : index) {
            indexes = indexes.add(0, ix);
        }
        return indexes;
    }

    @Override
    public Indexes add(int pos, Indexes index) {
        Index[] indices = StreamSupport
                .stream(index.spliterator(), false)
                .toArray(Index[]::new);
        return this.add(0, indices);
    }

    @Override
    public Indexes remove(int pos) {
        final int idx = pos * Long.BYTES;
        final int len = this.bytes.length;
        final int numMoved = len - idx - Long.BYTES;
        byte[] newElements;
        if (numMoved == 0) {
            newElements = Arrays.copyOf(this.bytes, len - Long.BYTES);
        } else {
            newElements = new byte[len - Long.BYTES];
            System.arraycopy(this.bytes, 0, newElements, 0, idx);
            System.arraycopy(this.bytes, idx + Long.BYTES, newElements, idx, numMoved);
        }
        return new ArrayIndexes(newElements);
    }

    @Override
    public Index get(final int pos) {
        final int idx = pos * Long.BYTES;
        return new LongIndex((long) LONG.get(this.bytes, idx));
    }

    @Override
    public ArrayIndexes sub(int from, int to) {
        from *= Long.BYTES;
        to *= Long.BYTES;
        return new ArrayIndexes(Arrays.copyOfRange(this.bytes, from, to));
    }
    @Override
    public InputStream bytes() {
        return new ByteArrayInputStream(this.bytes);
    }

    @Override
    public Iterator<Index> iterator() {
        return Spliterators.iterator(this.spliterator());
    }

    @Override
    public Spliterator<Index> spliterator() {
        return IntStream
                .iterate(0,
                        pos -> pos < this.size(),
                        pos -> pos + 1)
                .mapToObj(this::get)
                .spliterator();
    }

    @Override
    public String toString() {
        return Arrays.toString(this.bytes);
    }
}
