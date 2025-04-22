package me.sunmisc.btree;

import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.heap.Table;
import me.sunmisc.btree.heap.Values;
import me.sunmisc.btree.index.Index;

import java.util.List;
import java.util.Objects;


public interface Page extends Navigable {
    Split tryPushOrSplit(String key, String value);

    @Deprecated
    Indexes keys();

    @Deprecated
    Indexes children();


    default boolean isLeaf() {
        return children().size() == 0;
    }

    final class UnarySplit implements Split {
        private final Index origin;

        public UnarySplit(Index origin) {
            this.origin = origin;
        }

        @Override
        public Index median() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterable<Index> right() {
            return List.of();
        }

        @Override
        public long offset() {
            return this.origin.offset();
        }
    }

    final class RebalanceSplit implements Split {
        private final Index origin;
        private final List<Index> right;
        private final Indexes keys;

        public RebalanceSplit(Index origin, Indexes keys, List<Index> right) {
            this.origin = origin;
            this.right = right;
            this.keys = keys;
        }

        @Override
        public long offset() {
            return this.origin.offset();
        }

        @Override
        public Index median() {
            return keys.get(keys.size() >>> 1);
        }

        @Override
        public Iterable<Index> right() {
            return this.right;
        }
    }

    interface Split extends Index {

        Iterable<Index> right();

        Index median();
    }


    static int binSearch(String key, Indexes keys, Table table) {
        Values values = table.values();
        int low = 0;
        int high = keys.size() - 1;
        while (low <= high) {
            final int mid = low + high >>> 1;
            final String midVal = values.key(keys.get(mid));
            final int cmp = midVal.compareTo(key);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return -(low + 1);
    }
}
