package me.sunmisc.btree.cow;

import me.sunmisc.btree.Page;
import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.heap.Nodes;
import me.sunmisc.btree.heap.Table;
import me.sunmisc.btree.index.Index;

import java.util.List;
import java.util.Optional;

import static me.sunmisc.btree.cow.Tree.THRESHOLD;

public final class LeafPage implements Page {
    private final Table table;
    private final Nodes nodes;
    private final Indexes keys;

    public LeafPage(final Indexes keys, final Table table, final Nodes nodes) {
        this.table = table;
        this.keys = keys;
        this.nodes = nodes;
    }

    @Override
    public Split tryPushOrSplit(final String key, final Void dummy) {
        final Index value = this.table.allocate(key);
        final int index = this.binSearch(key);
        final Indexes copy = index >= 0
                ? this.keys.set(index, value)
                : this.keys.add(-index - 1, value);
        if (copy.size() < THRESHOLD) {
            return new Page.UnarySplit(this.nodes.allocate(
                    new LeafPage(copy, this.table, this.nodes)
            ), this.keys);
        }
        final int size = copy.size();
        final int mid = size >>> 1;
        final LeafPage left = new LeafPage(
                copy.sub(0, mid),
                this.table, this.nodes);
        final LeafPage right = new LeafPage(
                copy.sub(mid, copy.size()),
                this.table, this.nodes);
        return new RebalanceSplit(
                this.nodes.allocate(left),
                copy,
                List.of(this.nodes.allocate(right))
        );
    }

    @Override
    public Optional<Boolean> search(final String key) {
        final int index = this.binSearch(key);
        return index < 0 ? Optional.empty() : Optional.of(true);
    }

    @Override
    public Indexes children() {
        return Indexes.EMPTY;
    }
    @Override
    public Indexes keys() {
        return this.keys;
    }

    private int binSearch(final String key) {
        int low = 0;
        int high = this.keys.size() - 1;
        while (low <= high) {
            final int mid = low + high >>> 1;
            final String midVal = this.table.load(this.keys.get(mid));
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
