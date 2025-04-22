package me.sunmisc.btree.cow;

import me.sunmisc.btree.Page;
import me.sunmisc.btree.heap.*;
import me.sunmisc.btree.index.Index;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;

import static me.sunmisc.btree.cow.ImTree.THRESHOLD;

public final class LeafPage implements Page {
    private final Table table;
    private Indexes keys;

    public LeafPage(Table table, Indexes keys) {
        this.table = table;
        this.keys = keys;
    }
    @Override
    public Page.Split tryPushOrSplit(String key, String value) {
        Index pos = table.values().alloc(key, value);
        Nodes nodes = table.nodes();
        int index = Page.binSearch(key, keys, table);
        keys = index >= 0
                ? this.keys.set(index, pos)
                : this.keys.add(-index - 1, pos);
        return keys.size() < THRESHOLD
                ? new Page.UnarySplit(nodes.alloc(new LeafPage(table, keys)))
                : split();
    }

    public Page.Split split() {
        Nodes nodes = table.nodes();
        int size = this.keys.size();
        int mid = size >>> 1;
        LeafPage left = new LeafPage(table, this.keys.sub(0, mid));
        LeafPage right = new LeafPage(table, this.keys.sub(mid, this.keys.size()));
        return new Page.RebalanceSplit(
                nodes.alloc(left),
                this.keys,
                List.of(nodes.alloc(right))
        );
    }

    @Override
    public Optional<String> get(String key) {
        int index = Page.binSearch(key, keys, table);
        if (index < 0) {
            return Optional.empty();
        } else {
            return Optional.of(this.keys.get(index))
                    .map(e -> table.values().entry(e))
                    .map(Map.Entry::getValue);
        }
    }


    @Override
    public Indexes keys() {
        return keys;
    }

    @Override
    public Indexes children() {
        return new ArrayIndexes(new byte[0]);
    }

    @Override
    public Optional<Map.Entry<String, String>> first() {
        Values values = table.values();
        final int n = keys.size();
        return n > 0 ? Optional.of(values.entry(keys.get(0))) : Optional.empty();
    }

    @Override
    public Optional<Map.Entry<String, String>> last() {
        Values values = table.values();
        final int n = keys.size() - 1;
        return n >= 0 ? Optional.of(values.entry(keys.get(n))) : Optional.empty();
    }

    @Override
    public void forEach(final BiConsumer<String, String> action) {
        for (final Index x : this.keys) {
            final Map.Entry<String, String> e = this.table.values().entry(x);
            action.accept(e.getKey(), e.getValue());
        }
    }
}
