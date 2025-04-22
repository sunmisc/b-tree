package me.sunmisc.btree.cow;

import me.sunmisc.btree.Page;
import me.sunmisc.btree.Tree;
import me.sunmisc.btree.heap.ArrayIndexes;
import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.heap.Nodes;
import me.sunmisc.btree.heap.Table;
import me.sunmisc.btree.imm.Leaf;
import me.sunmisc.btree.index.Index;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.StreamSupport;

public final class ImTree implements Tree {
    public static final int THRESHOLD = 4;
    private final Table table;
    private final Index root;

    public ImTree(Table table, String key, String value) {
        Index first = table.values().alloc(key, value);
        Index root = table.nodes().alloc(new LeafPage(
                table,
                new ArrayIndexes().add(0, first))
        );
        this.table = table;
        this.root = root;
    }
    public ImTree(Table table, Index root) {
        this.table = table;
        this.root = root;
    }

    @Override
    public Tree put(String key, String value) {
        Nodes nodes = table.nodes();
        final Page start = nodes.find(root);
        final Page.Split split = start.tryPushOrSplit(key, value);
        final Index[] rights = StreamSupport
                .stream(split.right().spliterator(), false)
                .toArray(Index[]::new);
        Index newRoot = split;
        if (rights.length > 0) {
            newRoot = nodes.alloc(new InternalPage(
                    table,
                    new ArrayIndexes().add(0, split.median()),
                    new ArrayIndexes().add(0, split).add(1, rights)
            ));
        }
        return new ImTree(table, newRoot);
    }

    @Override
    public Tree remove(String key) {
        return new ImTree(table, root);
    }

    @Override
    public Optional<String> get(String key) {
        Nodes nodes = table.nodes();
        return nodes.find(root).get(key);
    }

    @Override
    public Optional<Map.Entry<String, String>> first() {
        Nodes nodes = table.nodes();
        return nodes.find(root).first();
    }

    @Override
    public Optional<Map.Entry<String, String>> last() {
        Nodes nodes = table.nodes();
        return nodes.find(root).last();
    }

    @Override
    public void forEach(BiConsumer<String, String> consumer) {
        Nodes nodes = table.nodes();
        nodes.find(root).forEach(consumer);
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

    public void printTree() {
        Nodes nodes = table.nodes();
        this.printTree(nodes.find(nodes.tail().get()), "");
    }

    private void printTree(Page node, String indent) {
        Nodes nodes = table.nodes();
        System.out.println(indent + StreamSupport
                .stream(node.keys().spliterator(), false)
                .map(e -> table.values().key(e)).toList() + " ");
        Indexes arrayIndexes = node.children();

        for(int i = 0; i < arrayIndexes.size(); ++i) {
            this.printTree(nodes.find(arrayIndexes.get(i)), indent + "  ");
        }
    }

    public static void main(String[] args) throws IOException {
        Table table = new Table("kek");
        try {
            Tree tree = new ImTree(table, table.root().get());

            tree = tree.put("r3", "31");

            System.out.println(tree.first());
            System.out.println(tree.last());
            System.out.println(tree);
        } finally {
     //       table.delete();
        }
    }
}
