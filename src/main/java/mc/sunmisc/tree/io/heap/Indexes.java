package mc.sunmisc.tree.io.heap;

import mc.sunmisc.tree.io.index.Index;

import java.io.InputStream;

public interface Indexes extends Iterable<Index> {

    int size();

    Index get(int pos);

    Indexes add(int pos, Index... index);

    Indexes set(int pos, Index index);

    Indexes sub(int offset, int count);

    InputStream bytes();

    Indexes EMPTY = new ArrayIndexes(0);
}
