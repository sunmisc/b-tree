package me.sunmisc.btree.cow;

import me.sunmisc.btree.Page;
import me.sunmisc.btree.heap.ArrayIndexes;
import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.heap.Nodes;
import me.sunmisc.btree.heap.Table;
import me.sunmisc.btree.index.Index;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.StreamSupport;

public final class Tree<K extends Comparable<K>, V>  {
    public static final int THRESHOLD = 4;
    private final Table table = new Table();
    private final Nodes nodes = new Nodes(this.table);
    private Page root;

    public Tree() {
        this.root = new LeafPage(new ArrayIndexes(), this.table, this.nodes);
    }

    public void insert(final String key, final Void value) {
        final Page.Split split = this.root.tryPushOrSplit(key, value);
        final Iterator<Index> child = split.iterator();
        if (child.hasNext()) {
            this.root = new InternalPage(
                    this.table,
                    this.nodes,
                    new ArrayIndexes().add(0, split.median()),
                    new ArrayIndexes().add(0, split, child.next())
            );
        } else {
            this.root = this.nodes.load(split);
        }
    }
    public Optional<Boolean> search(final String key) {
        return this.root.search(key);
    }

    public void printTree() {
        this.printTree(this.root, "");
    }

    private void printTree(final Page node, final String indent) {
        System.out.println(indent + StreamSupport
                .stream(node.keys().spliterator(), false)
                .map(this.table::load)
                .toList());
        final Indexes arrayIndexes = node.children();
        for (int i = 0; i < arrayIndexes.size(); ++i) {
            this.printTree(this.nodes.load(arrayIndexes.get(i)), indent + "  ");
        }
    }

    public static void main(final String[] args) throws FileNotFoundException {
        final Tree<Integer, String> tree = new Tree<>();
        for (int i = 0; i < 150; ++i) {
            tree.insert(i+"", null);
        }
        tree.printTree();
        for (int i = 0; i < 150; ++i) {
            System.out.println(tree.search(i+""));
        }
        System.out.println(tree.search("pizdec"));
    }
}