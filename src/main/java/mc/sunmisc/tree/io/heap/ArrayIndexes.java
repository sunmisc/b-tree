package mc.sunmisc.tree.io.heap;

import mc.sunmisc.tree.io.index.Index;
import mc.sunmisc.tree.io.index.LongIndex;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

public final class ArrayIndexes implements Indexes {
    private static final VarHandle LONG = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
    private final byte[] bytes;

    public ArrayIndexes(final int size) {
        this(new byte[size * Long.BYTES]);
    }

    public ArrayIndexes(final byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public int size() {
        return Math.ceilDiv(bytes.length, Long.BYTES);
    }

    @Override
    public ArrayIndexes set(int pos, Index value) {
        final int idx = pos * Long.BYTES;
        final byte[] array = bytes.clone();
        LONG.set(array, idx, value.offset());
        return new ArrayIndexes(array);
    }

    @Override
    public ArrayIndexes add(int pos, Index... values) {
        int space = values.length * Long.BYTES;

        final int idx = pos * Long.BYTES;
        final int len = bytes.length;
        final int numMoved = len - idx;

        byte[] newElements;
        if (numMoved == 0) {
            newElements = Arrays.copyOf(bytes, len + space);
        } else {
            newElements = new byte[len + space];
            System.arraycopy(bytes, 0, newElements, 0, idx);
            System.arraycopy(bytes, idx,
                    newElements, idx + space,
                    numMoved);
        }
        for (int i = 0; i < values.length; ++i) {
            LONG.set(newElements, idx + (i * Long.BYTES), values[i].offset());
        }
        return new ArrayIndexes(newElements);
    }

    @Override
    public Index get(int pos) {
        final int idx = pos * Long.BYTES;
        return new LongIndex((long) LONG.get(bytes, idx));
    }

    @Override
    public ArrayIndexes sub(int from, int to) {
        from *= Long.BYTES;
        to *= Long.BYTES;
        return new ArrayIndexes(Arrays.copyOfRange(bytes, from, to));
    }
    @Override
    public InputStream bytes() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public String toString() {
        return Arrays.toString(bytes);
    }

    @Override
    public Iterator<Index> iterator() {
        return Spliterators.iterator(spliterator());
    }

    @Override
    public Spliterator<Index> spliterator() {
        return IntStream
                .iterate(0,
                        pos -> pos < size(),
                        pos -> pos + 1)
                .mapToObj(this::get)
                .spliterator();
    }
}
