package me.sunmisc.btree.cow;

import me.sunmisc.btree.Page;
import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.heap.Nodes;
import me.sunmisc.btree.heap.Table;
import me.sunmisc.btree.index.Index;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static me.sunmisc.btree.cow.FlushTree.THRESHOLD;

public final class LeafPage implements Page {
    private final Table table;
    private final Nodes nodes;
    private Indexes keys;

    public LeafPage(Indexes keys, Table table, Nodes nodes) {
        this.table = table;
        this.keys = keys;
        this.nodes = nodes;
    }
    @Override
    public Page.Split tryPushOrSplit(String key, String value) {
        Index pos = this.table.alloc(key, value);
        int index = Page.binSearch(key, keys, table);
        keys = index >= 0
                ? this.keys.set(index, pos)
                : this.keys.add(-index - 1, pos);
        return keys.size() < THRESHOLD
                ? new Page.UnarySplit(this.nodes.alloc(
                        new LeafPage(keys, this.table, this.nodes)))
                : split();
    }

    public Page.Split split() {
        int size = this.keys.size();
        int mid = size >>> 1;
        LeafPage left = new LeafPage(
                this.keys.sub(0, mid),
                this.table, this.nodes);
        LeafPage right = new LeafPage(
                this.keys.sub(mid, this.keys.size()),
                this.table, this.nodes);
        return new Page.RebalanceSplit(
                this.nodes.alloc(left),
                this.keys,
                List.of(this.nodes.alloc(right))
        );
    }
    @Override
    public Optional<String> get(String key) {
        int index = Page.binSearch(key, keys, table);
        if (index < 0) {
            return Optional.empty();
        } else {
            return Optional.of(this.keys.get(index))
                    .map(table::entry)
                    .map(Map.Entry::getValue);
        }

    }

    @Override
    public Optional<Map.Entry<String, String>> first() {
        final int n = keys.size();
        return n > 0 ? Optional.of(table.entry(keys.get(0))) : Optional.empty();
    }

    @Override
    public Optional<Map.Entry<String, String>> last() {
        final int n = keys.size() - 1;
        return n >= 0 ? Optional.of(table.entry(keys.get(n))) : Optional.empty();
    }

    @Override
    public InputStream delta() throws IOException {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final DataOutputStream data = new DataOutputStream(out);
             final InputStream ks = this.keys.bytes()) {
            final byte[] bks = ks.readAllBytes();
            data.writeInt(bks.length);
            data.write(bks);
            data.writeInt(0);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    public void forEach(final BiConsumer<String, String> action) {
        for (final Index x : this.keys) {
            final Map.Entry<String, String> e = this.table.entry(x);
            action.accept(e.getKey(), e.getValue());
        }
    }
}
