package me.sunmisc.btree.imm;

import java.util.ArrayList;
import java.util.List;

public class BTree {
    private Node root;

    public BTree() {
        this.root = new LeafNode(new ArrayList<>());
    }

    public void insert(String key, String value) {
        Node newRoot = root.insert(key, value);
        if (newRoot instanceof SplitResult split) {
            List<Node> newChildren = List.of(split.leftNode, split.rightNode);
            root = new InternalNode(List.of(split.medianKey), newChildren);
        } else {
            root = newRoot;
        }
    }

    public void delete(String key) {
        try {
            Node newRoot = root.delete(key);
            // print();
            if (newRoot.getSize() < 2 && !(newRoot instanceof LeafNode)) {
                root = newRoot.children.getFirst();
            } else {
                root = newRoot;
            }
        } catch (Exception ex) {

        }
        // print();

    }


    public String search(String key) {
        return root.search(key);
    }

    public String firstKey() {
        return root.smallestKey();
    }

    public Node getRoot() {
        return root;
    }

    public void print() {
        BTreePrinter.printTree(root, 0);
    }

    public static class BTreePrinter {
        public static void printTree(Node node, int level) {
            if (node instanceof LeafNode) {
                LeafNode leaf = (LeafNode) node;
                System.out.println("Level " + level + " (Leaf): Keys = " + leaf.keys);
            } else if (node instanceof InternalNode internal) {
                System.out.println("Level " + level + " (Internal): Keys = " + internal.keys);
                for (Node child : internal.children) {
                    printTree(child, level + 1);
                }
            }
        }
    }
}