package me.sunmisc.btree.heap;

import me.sunmisc.btree.index.Index;

import java.io.InputStream;

public interface Indexes extends Iterable<Index> {

    int size();

    Index get(int pos);

    Indexes add(int pos, Index... index);

    Indexes addAll(Indexes index);

    Indexes add(int pos, Indexes index);

    Indexes remove(int pos);

    Indexes set(int pos, Index index);

    Indexes sub(int offset, int count);

    InputStream bytes();
}
