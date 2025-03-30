package mc.sunmisc.tree.memory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class BPlusTree<K extends Comparable<K>, V>  {
    public static final int THRESHOLD = 4;
    private Node<K, V> root;

    public BPlusTree() {
        root = new LeafNode<>();
    }

    public void insert(K key, V value) {
        Node.Split<K, V> split = root.tryPushOrSplit(key, value);
        if (split != null) {
            root = new InternalNode<>(
                    List.of(split.mid()),
                    List.of(split.left(), split.right())
            );
        }
    }

    public void printTree() {
        printTree(root, "");
    }

    private void printTree(Node<K, V> node, String indent) {
        System.out.println(indent + node.keys().toList());
        for (Node<K, V> child : node) {
            printTree(child, indent + "  ");
        }
    }
    public Optional<V> search(K key) {
        return root.search(key);
    }
    public Stream<V> between(K from, K to) {
        return root.between(from, to);
    }
    public static void main(String[] args) {
        BPlusTree<Integer, String> tree = new BPlusTree<>();
        for (int i = 0; i < 15; ++i) {
            tree.insert(i, i+"");
        }
        tree.printTree();
        for (int i = 0; i < 15; ++i) {
            System.out.println(tree.search(i));
        }
        System.out.println(tree.between(3, 9).toList());
    }
}
