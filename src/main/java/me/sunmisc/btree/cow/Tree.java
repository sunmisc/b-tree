//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package me.sunmisc.btree.cow;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import me.sunmisc.btree.Page;
import me.sunmisc.btree.heap.ArrayIndexes;
import me.sunmisc.btree.heap.Indexes;
import me.sunmisc.btree.heap.Nodes;
import me.sunmisc.btree.heap.Table;
import me.sunmisc.btree.index.Index;

public final class Tree<K extends Comparable<K>, V> {
    public static final int THRESHOLD = 4;
    private final Lock lock = new ReentrantLock();
    private final Table keys;
    private final Nodes nodes;

    public Tree(Nodes nodes, Table keys) {
        this.nodes = nodes;
        this.keys = keys;
    }

    public void insert(String key, String value) {
        this.lock.lock();
        try {
            this.nodes.tail().ifPresentOrElse((t) -> {
                Page page = this.nodes.find(t);
                Page.Split split = page.tryPushOrSplit(key, value);
                Iterator<Index> right = split.iterator();
                if (right.hasNext()) {
                    Index p = right.next();
                    var x = new InternalPage(
                            this.keys,
                            this.nodes,
                            new ArrayIndexes().add(0, split.median()),
                            new ArrayIndexes().add(0, split, p)
                    );
                    nodes.alloc(x);
                }
            }, () -> {
                Index first = this.keys.alloc(key, value);
                this.nodes.alloc(new LeafPage(
                        new ArrayIndexes().add(0, first),
                        this.keys, this.nodes)
                );
            });
        } finally {
            this.lock.unlock();
        }

    }

    public Optional<String> search(String key) {
        return nodes.tail().map(nodes::find).flatMap((e) -> e.search(key));
    }

    public void printTree() {
        this.nodes.tail().ifPresent((t) -> this.printTree(this.nodes.find(t), ""));
    }

    private void printTree(Page node, String indent) {
        Stream<Index> var10002 = StreamSupport.stream(node.keys().spliterator(), false);
        System.out.println(indent + var10002.map(keys::key).toList() + " " +
                (node instanceof LeafPage));
        Indexes arrayIndexes = node.children();

        for(int i = 0; i < arrayIndexes.size(); ++i) {
            this.printTree(this.nodes.find(arrayIndexes.get(i)), indent + "  ");
        }

    }

    public static void main(String[] args) {
        Table table = new Table();
        Nodes nodes1 = new Nodes(table);
        try {
            Tree<Integer, String> tree = new Tree<>(nodes1, table);

            for (int i = 0; i < 15; ++i) {
                tree.insert("" + i, "kek " + i);
            }

            List<String> k = new ArrayList<>();
            tree.nodes.find(tree.nodes.tail().get()).traverse(k);
            System.out.println(k);
            tree.printTree();

            for (int i = 0; i < 15; ++i) {
                System.out.println(tree.search("" + i));
            }

            System.out.println(tree.search("-12"));
        } finally {
            nodes1.delete();
            table.delete();
        }
    }
}
