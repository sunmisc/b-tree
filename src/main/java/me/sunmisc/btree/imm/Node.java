package me.sunmisc.btree.imm;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    protected List<String> keys;
    protected List<Node> children;
    protected int order;

    public Node(int order, List<String> keys, List<Node> children) {
        this.order = order;
        this.keys = keys != null ? new ArrayList<>(keys) : new ArrayList<>();
        this.children = children != null ? new ArrayList<>(children) : new ArrayList<>();
    }

    public int getSize() {
        return children.size();
    }

    public abstract int getMinChildren();

    public abstract int getMaxChildren();

    public boolean satisfiesMinChildren() {
        return getSize() >= getMinChildren();
    }

    public boolean satisfiesMaxChildren() {
        return getSize() <= getMaxChildren();
    }

    public Node tail() {
        return createNewNode(order, Utils.tail(keys), Utils.tail(children));
    }

    public Node init() {
        return createNewNode(order, Utils.copy(keys), Utils.copy(children));
    }

    public boolean shouldSplit() {
        return !satisfiesMaxChildren();
    }

    public abstract Node merge(Node otherNode);

    public abstract List<Node> stealFirstKeyFrom(Node rightSibling);
    public abstract List<Node> giveLastKeyTo(Node rightSibling);

    protected abstract Node createNewNode(int order, List<String> keys, List<Node> children);

    public abstract Node insert(boolean[] didChange, String key, String value);

    public abstract Node delete(boolean[] didChange, String key);

    public abstract String smallestKey();

    public abstract String search(String key);
}