package me.sunmisc.btree.heap;

import me.sunmisc.btree.index.Index;

import java.io.InputStream;

public interface Indexes extends Iterable<Index> {

    Indexes EMPTY = new ArrayIndexes();

    int size();

    Index get(int pos);

    Indexes add(int pos, Index... index);

    Indexes set(int pos, Index index);

    Indexes sub(int offset, int count);

    InputStream bytes();
}
