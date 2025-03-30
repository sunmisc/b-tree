package mc.sunmisc.tree.io;

import mc.sunmisc.tree.io.heap.ArrayIndexes;
import mc.sunmisc.tree.io.heap.Indexes;
import mc.sunmisc.tree.io.index.Index;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public interface Page {

    Split tryPushOrSplit(String key, Void value);

    Optional<Boolean> search(String key);

    Indexes children();

    Indexes keys();

    final class UnarySplit implements Split {

        private final Index origin;

        public UnarySplit(Index origin) {
            this.origin = origin;
        }

        @Override
        public Index median() {
            return null;
        }

        @Override
        public Iterator<Index> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public long offset() {
            return origin.offset();
        }
    }

    final class RebalanceSplit implements Split {
        private final Index origin;
        private final List<Index> list;
        private final Indexes keys;

        public RebalanceSplit(Index origin, Indexes keys, List<Index> ids) {
            this.origin = origin;
            this.list = ids;
            this.keys = keys;
        }

        @Override
        public long offset() {
            return origin.offset();
        }

        @Override
        public Index median() {
            int mid = keys.size() >>> 1;
            return keys.get(mid);
        }

        @Override
        public String toString() {
            return keys + " = " + list;
        }

        @Override
        public Iterator<Index> iterator() {
            return list.iterator();
        }
    }

    interface Split extends Iterable<Index>, Index {

        Index median();
    }

}
