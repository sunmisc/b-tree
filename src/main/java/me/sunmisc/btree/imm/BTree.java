package me.sunmisc.btree.imm;

import java.util.ArrayList;
import java.util.List;

import static me.sunmisc.btree.imm.Constants.MIN_ROOT_CHILDREN;

public class BTree {
    private Node root;

    public BTree() {
        this.root = new Leaf(Constants.ORDER, new ArrayList<>());
    }

    public void insert(String key, String value) {
        if (search(key) != null) {
            //   System.out.println("Key already exists, skipping: " + key);
            return;
        }
        boolean[] didChange = new boolean[1];
        Node newRoot = root.insert(didChange, key, value);
        if (!didChange[0]) {
            return;
        }
        if (newRoot instanceof SplitResult split) {
            List<Node> newChildren = List.of(split.leftNode, split.rightNode);
            root = new InternalNode(Constants.ORDER, List.of(split.medianKey), newChildren);
        } else {
            root = newRoot;
        }
    }

    public void delete(String key) {
        boolean[] didChange = new boolean[1];
        Node newRoot = root.delete(didChange, key);
        // print();

        // Если корень стал пустым и это не лист
        if (newRoot.getSize() == 0 && !(newRoot instanceof Leaf)) {
            if (newRoot.children.isEmpty()) {
                // Если нет детей, создаем новый пустой лист
                root = new Leaf(Constants.ORDER, new ArrayList<>());
            } else {
                // Если есть дети, первый ребенок становится новым корнем
                root = newRoot.children.getFirst();
            }
        }
        // Если в корне остался только один ключ и это не лист
        else if (newRoot.getSize() < 2 && !(newRoot instanceof Leaf)) {
            root = newRoot.children.getFirst();
        } else {
            root = newRoot;
        }

       // print();

    }


    public String search(String key) {
        return root.search(key);
    }

    public Node getRoot() {
        return root;
    }
    public void print() {
        BTreePrinter.printTree(root, 0);
    }

    public static class BTreePrinter {
        public static void printTree(Node node, int level) {
            if (node instanceof Leaf) {
                Leaf leaf = (Leaf) node;
                System.out.println("Level " + level + " (Leaf): Keys = " + leaf.keys);
            } else if (node instanceof InternalNode internal) {
                System.out.println("Level " + level + " (Internal): Keys = " + internal.keys);
                for (Node child : internal.children) {
                    printTree(child, level + 1);
                }
            }
        }
    }

    public static void main(String[] args) {
        BTree tree = new BTree();

        // Insert key-value pairs
        System.out.println("Inserting keys: a, b, c, d, e, f");
        tree.insert("a", "apple");
        tree.insert("b", "banana");
        tree.insert("c", "cherry");
        tree.insert("d", "date");
        tree.insert("e", "elderberry");
        tree.insert("f", "fig");
        System.out.println("Tree after insertions:");
        BTreePrinter.printTree(tree.getRoot(), 0);

        // Search for keys
        System.out.println("\nSearching for keys:");
        System.out.println("Key 'b': " + tree.search("b")); // Should print "banana"
        System.out.println("Key 'x': " + tree.search("x")); // Should print null

        // Delete a key
        System.out.println("\nDeleting key: c");
        tree.delete("c");
        System.out.println("Tree after deleting key 'c':");
        BTreePrinter.printTree(tree.getRoot(), 0);

        // Search again
        System.out.println("\nSearching after deletion:");
        System.out.println("Key 'c': " + tree.search("c")); // Should print null
        System.out.println("Key 'e': " + tree.search("e")); // Should print "elderberry"
    }
}