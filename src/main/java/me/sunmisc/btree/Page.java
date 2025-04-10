//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.sunmisc.btree;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.index.Index;

public interface Page {
    Split tryPushOrSplit(String var1, String var2);

    Optional<String> search(String var1);

    Indexes children();

    Indexes keys();

    void traverse(List<String> var1);

    public static final class UnarySplit implements Split {
        private final Index origin;
        private final Indexes keys;

        public UnarySplit(Index origin, Indexes keys) {
            this.origin = origin;
            this.keys = keys;
        }

        public Indexes keys() {
            return this.keys;
        }

        public Iterator<Index> iterator() {
            return Collections.emptyIterator();
        }

        public long offset() {
            return this.origin.offset();
        }
    }

    public static final class RebalanceSplit implements Split {
        private final Index origin;
        private final List<Index> right;
        private final Indexes keys;

        public RebalanceSplit(Index origin, Indexes keys, List<Index> right) {
            this.origin = origin;
            this.right = right;
            this.keys = keys;
        }

        public long offset() {
            return this.origin.offset();
        }

        public Indexes keys() {
            return this.keys;
        }

        public Iterator<Index> iterator() {
            return this.right.iterator();
        }
    }

    public interface Split extends Iterable<Index>, Index {
        Indexes keys();

        default Index median() {
            Indexes data = this.keys();
            int mid = data.size() >>> 1;
            return this.keys().get(mid);
        }
    }
}
