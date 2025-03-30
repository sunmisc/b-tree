package mc.sunmisc.tree.io.heap;

import mc.sunmisc.tree.io.index.Index;

public interface Alloc<V> {

    Index alloc(Data value);
}
