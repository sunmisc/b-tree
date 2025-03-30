package mc.sunmisc.tree.io;

import mc.sunmisc.tree.io.heap.ArrayIndexes;
import mc.sunmisc.tree.io.heap.Indexes;
import mc.sunmisc.tree.io.heap.Nodes;
import mc.sunmisc.tree.io.heap.Table;
import mc.sunmisc.tree.io.index.Index;

import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.StreamSupport;

public final class BPlusTreeIo<K extends Comparable<K>, V>  {
    public static final int THRESHOLD = 4;
    private final Table table = new Table();
    private final Nodes nodes = new Nodes(table);
    private Page root;

    public BPlusTreeIo() {
        this.root = new LeafPage(new ArrayIndexes(0), table, nodes);
    }

    public void insert(String key, Void value) {
        Page.Split split = root.tryPushOrSplit(key, value);
        Iterator<Index> child = split.iterator();
        if (child.hasNext()) {
            ArrayIndexes ks = new ArrayIndexes(0);
            ks = ks.add(0, split.median());
            ArrayIndexes childs = new ArrayIndexes(0);
            childs = childs.add(0, split, child.next());

            root = new InternalPage(
                    table,
                    nodes,
                    ks,
                    childs
            );
        } else {
            root = nodes.load(split);
        }
    }
    public Optional<Boolean> search(String key) {
        return root.search(key);
    }

    public void printTree() {
        printTree(root, "");
    }

    private void printTree(Page node, String indent) {
        System.out.println(indent + StreamSupport
                .stream(node.keys().spliterator(), false)
                .map(table::load)
                .toList());
        Indexes arrayIndexes = node.children();
        for (int i = 0; i < arrayIndexes.size(); ++i) {
            printTree(nodes.load(arrayIndexes.get(i)), indent + "  ");
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        BPlusTreeIo<Integer, String> tree = new BPlusTreeIo<>();
        for (int i = 0; i < 15; ++i) {
            tree.insert(i+"", null);
        }
        tree.printTree();
        for (int i = 0; i < 15; ++i) {
            System.out.println(tree.search(i+""));
        }
        System.out.println(tree.search("pizdec"));
    }
}