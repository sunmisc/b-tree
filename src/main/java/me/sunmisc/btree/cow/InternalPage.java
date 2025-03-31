package me.sunmisc.btree.cow;

import me.sunmisc.btree.Page;
import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.heap.Nodes;
import me.sunmisc.btree.heap.Table;
import me.sunmisc.btree.index.Index;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static me.sunmisc.btree.cow.Tree.THRESHOLD;

public final class InternalPage implements Page {
    private final Nodes nodes;
    private final Table table;
    private Indexes keys;
    private Indexes children;

    public InternalPage(final Table table, final Nodes nodes, final Indexes keys, final Indexes children) {
        this.table = table;
        this.nodes = nodes;
        this.keys = keys;
        this.children = children;
    }

    @Override
    public Split tryPushOrSplit(final String key, final Void value) {
        final int idx = this.binSearch(key);
        final int index = idx < 0 ? -idx - 1 : idx;
        final Page.Split split = this.nodes
                .load(this.children.get(index))
                .tryPushOrSplit(key, value);
        this.children = this.children.set(index, split);
        final Iterator<Index> rights = split.iterator();
        if (rights.hasNext()) {
            final Index[] inc = StreamSupport
                    .stream(split.spliterator(), false)
                    .toArray(Index[]::new);
            this.children = children.add(index + 1, inc);
            this.keys = this.keys.add(index, split.median());
        }
        if (keys.size() < THRESHOLD) {
            return new UnarySplit(
                    this.nodes.allocate(
                            new InternalPage(this.table, this.nodes,
                                    this.keys,
                                    this.children
                            )
                    ), this.keys
            );
        }
        final int mid = this.keys.size() >>> 1;
        final Page left = new InternalPage(this.table, this.nodes,
                this.keys.sub(0, mid),
                this.children.sub(0, mid + 1));
        final Page right = new InternalPage(this.table, this.nodes,
                this.keys.sub(mid + 1, this.keys.size()),
                this.children.sub(mid + 1, children.size())
        );
        return new RebalanceSplit(
                this.nodes.allocate(left),
                this.keys,
                List.of(this.nodes.allocate(right))
        );
    }

    private Page.Split split() {
        final int mid = this.keys.size() >>> 1;
        final Page left = new InternalPage(this.table, this.nodes,
                this.keys.sub(0, mid),
                this.children.sub(0, mid + 1));
        final Page right = new InternalPage(this.table, this.nodes,
                this.keys.sub(mid + 1, this.keys.size()),
                this.children.sub(mid + 1, this.children.size())
        );
        return new Page.RebalanceSplit(
                this.nodes.allocate(left),
                this.keys,
                List.of(this.nodes.allocate(right))
        );
    }

    @Override
    public Optional<Boolean> search(final String key) {
        int index = this.binSearch(key);
        index = index >= 0 ? index + 1 : -index - 1;
        return this.nodes.load(this.children.get(index)).search(key);
    }

    @Override
    public Indexes keys() {
        return this.keys;
    }
    @Override
    public Indexes children() {
        return this.children;
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
