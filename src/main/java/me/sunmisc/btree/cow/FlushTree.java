package me.sunmisc.btree.cow;

import me.sunmisc.btree.Page;
import me.sunmisc.btree.Tree;
import me.sunmisc.btree.heap.ArrayIndexes;
import me.sunmisc.btree.heap.Nodes;
import me.sunmisc.btree.heap.Table;
import me.sunmisc.btree.index.Index;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.StreamSupport;

public final class FlushTree implements Tree {
    public static final int THRESHOLD = 4;
    private final Table keys;
    private final Nodes nodes;

    public FlushTree(Nodes nodes, Table keys) {
        this.nodes = nodes;
        this.keys = keys;
    }

    @Override
    public Tree put(String key, String value) {
        this.nodes.tail().ifPresentOrElse(t -> {
            final Page.Split split = this.nodes.find(t).tryPushOrSplit(key, value);
            final Index[] rights = StreamSupport
                    .stream(split.right().spliterator(), false)
                    .toArray(Index[]::new);
            if (rights.length > 0) {
                nodes.alloc(new InternalPage(
                        this.keys,
                        this.nodes,
                        new ArrayIndexes().add(0, split.median()),
                        new ArrayIndexes().add(0, split).add(1, rights)
                ));
            }
        }, () -> {
            Index first = this.keys.alloc(key, value);
            this.nodes.alloc(new LeafPage(
                    new ArrayIndexes().add(0, first),
                    this.keys, this.nodes)
            );
        });
        return this;
    }

    @Override
    public Tree remove(String key) {
        return this;
    }

    @Override
    public Optional<String> get(String key) {
        return this.nodes.tail().map(nodes::find).flatMap((e) -> e.get(key));
    }

    @Override
    public Optional<Map.Entry<String, String>> first() {
        return this.nodes.tail().map(nodes::find).flatMap(Page::first);
    }

    @Override
    public Optional<Map.Entry<String, String>> last() {
        return this.nodes.tail().map(nodes::find).flatMap(Page::last);
    }

    @Override
    public void forEach(BiConsumer<String, String> consumer) {
        this.nodes.tail().ifPresent(t -> nodes.find(t).forEach(consumer));
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        final List<Map.Entry<String, String>> accumulate = new LinkedList<>();
        forEach((k,v) -> accumulate.add(Map.entry(k, v)));
        return accumulate.iterator();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        forEach((k,v) -> joiner.add(String.format("%s=%s", k, v)));
        return joiner.toString();
    }

    public static void main(String[] args) {
        Table table = new Table();
        Nodes nodes1 = new Nodes(table);
        try {
            Tree tree = new FlushTree(nodes1, table);

            for (int i = 0; i < 15; ++i) {
                tree = tree.put("" + i, "kek" + i);
            }

            System.out.println(tree.first());
            System.out.println(tree.last());
            System.out.println(tree);

            for (int i = 0; i < 15; ++i) {
                System.out.println(tree.get("" + i));
            }

            System.out.println(tree.get("-12"));
        } finally {
            nodes1.delete();
            table.delete();
        }
    }
}
