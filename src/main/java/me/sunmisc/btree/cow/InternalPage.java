package me.sunmisc.btree.cow;

import me.sunmisc.btree.Page;
import me.sunmisc.btree.heap.ArrayIndexes;
import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.heap.Nodes;
import me.sunmisc.btree.heap.Table;
import me.sunmisc.btree.imm.Constants;
import me.sunmisc.btree.imm.Leaf;
import me.sunmisc.btree.index.Index;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.StreamSupport;

import static me.sunmisc.btree.cow.ImTree.THRESHOLD;

public final class InternalPage implements Page {
    private final Table table;
    // todo: final
    private Indexes keys;
    private Indexes children;

    public InternalPage(Table table, Indexes keys, Indexes children) {
        this.table = table;
        this.keys = keys;
        this.children = children;
    }

    @Override
    public Page.Split tryPushOrSplit(String key, String value) {
        Nodes nodes = table.nodes();
        int idx = Page.binSearch(key, keys, table);
        int index = idx < 0 ? -idx - 1 : idx;
        final Page.Split split = nodes
                .find(this.children.get(index))
                .tryPushOrSplit(key, value);
        this.children = this.children.set(index, split);
        final Index[] rights = StreamSupport
                .stream(split.right().spliterator(), false)
                .toArray(Index[]::new);
        if (rights.length > 0) {
            this.children = this.children.add(index + 1, rights);
            this.keys = this.keys.add(index, split.median());
        }
        if (keys.size() < THRESHOLD) {
            return new Page.UnarySplit(
                    nodes.alloc(
                            new InternalPage(table,
                                    this.keys,
                                    this.children
                            )
                    ));
        } else {
            return split();
        }
    }

    @Override
    public Indexes keys() {
        return keys;
    }

    @Override
    public Indexes children() {
        return children;
    }

    private Page.Split split() {
        Nodes nodes = table.nodes();

        int mid = this.keys.size() >>> 1;
        Page left = new InternalPage(table,
                this.keys.sub(0, mid),
                this.children.sub(0, mid + 1));
        Page right = new InternalPage(table,
                this.keys.sub(mid + 1, this.keys.size()),
                this.children.sub(mid + 1, this.children.size())
        );
        return new Page.RebalanceSplit(
                nodes.alloc(left),
                this.keys,
                List.of(nodes.alloc(right))
        );
    }


    @Override
    public Optional<String> get(String key) {
        Nodes nodes = table.nodes();
        int index = Page.binSearch(key, keys, table);
        index = index >= 0 ? index + 1 : -index - 1;
        return nodes.find(this.children.get(index)).get(key);
    }

    @Override
    public Optional<Map.Entry<String, String>> first() {
        final int n = children.size();
        Nodes nodes = table.nodes();
        return n > 0 ? nodes.find(children.get(0)).first() : Optional.empty();
    }

    @Override
    public Optional<Map.Entry<String, String>> last() {
        final int n = children.size() - 1;
        Nodes nodes = table.nodes();
        return n >= 0 ? nodes.find(children.get(n)).last() : Optional.empty();
    }

    @Override
    public void forEach(final BiConsumer<String, String> result) {
        Nodes nodes = table.nodes();
        for (final Index child : this.children) {
            final Page page = nodes.find(child);
            page.forEach(result);
        }
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}