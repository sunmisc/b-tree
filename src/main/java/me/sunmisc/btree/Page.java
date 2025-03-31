package me.sunmisc.btree;

import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.index.Index;

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
        private final Indexes keys;

        public UnarySplit(final Index origin, final Indexes keys) {
            this.origin = origin;
            this.keys = keys;
        }

        @Override
        public Indexes keys() {
            return this.keys;
        }

        @Override
        public Iterator<Index> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public long offset() {
            return this.origin.offset();
        }
    }

    final class RebalanceSplit implements Split {
        private final Index origin;
        private final List<Index> list;
        private final Indexes keys;

        public RebalanceSplit(final Index origin, final Indexes keys, final List<Index> ids) {
            this.origin = origin;
            this.list = ids;
            this.keys = keys;
        }

        @Override
        public long offset() {
            return this.origin.offset();
        }

        @Override
        public Indexes keys() {
            return this.keys;
        }

        @Override
        public Iterator<Index> iterator() {
            return this.list.iterator();
        }
    }

    interface Split extends Iterable<Index>, Index {

        Indexes keys();

        default Index median() {
            final Indexes data = this.keys();
            final int mid = data.size() >>> 1;
            return this.keys().get(mid);
        }
    }

}
