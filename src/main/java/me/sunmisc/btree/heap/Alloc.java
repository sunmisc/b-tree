package me.sunmisc.btree.heap;

import me.sunmisc.btree.index.Index;

public interface Alloc<V> {

    Index alloc(Data value);
}
