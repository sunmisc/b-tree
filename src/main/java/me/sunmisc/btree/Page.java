package me.sunmisc.btree;

import me.sunmisc.btree.heap.Data;
import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.heap.Table;
import me.sunmisc.btree.index.Index;

import java.util.List;
import java.util.Objects;

public interface Page extends Data, Navigable {
    Split tryPushOrSplit(String key, String value);

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
        int i = 0;
        int xx = Integer.parseInt(key);
        for (; i < keys.size(); ++i) {
            Index index = keys.get(i);
            int val = Integer.parseInt(table.key(index));
            if (Objects.equals(xx, val)) {
                return i;
            } else if (Integer.parseInt(key) < val) {
                break;
            }
        }
        return -(i + 1);
    }

    static int binSearch1(String key, Indexes keys, Table table) {
        int low = 0;
        int high = keys.size() - 1;
        while (low <= high) {
            final int mid = low + high >>> 1;
            final String midVal = table.key(keys.get(mid));
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
