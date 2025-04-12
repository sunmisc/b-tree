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
import java.util.stream.StreamSupport;

import static me.sunmisc.btree.cow.FlushTree.THRESHOLD;

public final class InternalPage implements Page {
    private final Nodes nodes;
    private final Table table;
    private Indexes keys;
    private Indexes children;

    public InternalPage(Table table, Nodes nodes, Indexes keys, Indexes children) {
        this.table = table;
        this.nodes = nodes;
        this.keys = keys;
        this.children = children;
    }

    @Override
    public Page.Split tryPushOrSplit(String key, String value) {
        int idx = Page.binSearch(key, keys, table);
        int index = idx < 0 ? -idx - 1 : idx;
        final Page.Split split = this.nodes
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
                    this.nodes.alloc(
                            new InternalPage(this.table,
                                    this.nodes,
                                    this.keys,
                                    this.children
                            )
                    ));
        } else {
            return split();
        }
    }

    private Page.Split split() {
        int mid = this.keys.size() >>> 1;
        Page left = new InternalPage(this.table, this.nodes,
                this.keys.sub(0, mid),
                this.children.sub(0, mid + 1));
        Page right = new InternalPage(this.table, this.nodes,
                this.keys.sub(mid + 1, this.keys.size()),
                this.children.sub(mid + 1, this.children.size())
        );
        return new Page.RebalanceSplit(
                this.nodes.alloc(left),
                this.keys,
                List.of(this.nodes.alloc(right))
        );
    }

    @Override
    public Optional<String> get(String key) {
        int index = Page.binSearch(key, keys, table);
        index = index >= 0 ? index + 1 : -index - 1;
        return this.nodes.find(this.children.get(index)).get(key);
    }

    @Override
    public Optional<Map.Entry<String, String>> first() {
        final int n = children.size();
        return n > 0 ? nodes.find(children.get(0)).first() : Optional.empty();
    }

    @Override
    public Optional<Map.Entry<String, String>> last() {
        final int n = children.size() - 1;
        return n >= 0 ? nodes.find(children.get(n)).last() : Optional.empty();
    }

    @Override
    public void forEach(final BiConsumer<String, String> result) {
        for (final Index child : this.children) {
            final Page page = this.nodes.find(child);
            page.forEach(result);
        }
    }

    @Override
    public InputStream delta() throws IOException {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final DataOutputStream data = new DataOutputStream(out);
             final InputStream ks = this.keys.bytes();
             final InputStream ch = this.children.bytes()) {
            final byte[] bks = ks.readAllBytes();
            final byte[] chs = ch.readAllBytes();
            data.writeInt(bks.length);
            data.write(bks);
            data.writeInt(chs.length);
            data.write(chs);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
