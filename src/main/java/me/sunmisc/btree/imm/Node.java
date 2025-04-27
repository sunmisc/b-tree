package me.sunmisc.btree.imm;

import java.util.List;

public abstract class Node {
    protected List<String> keys;
    protected List<Node> children;

    public Node(List<String> keys, List<Node> children) {
        this.keys = keys;
        this.children = children;
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

    @Deprecated
    public Node tail() {
        return createNewNode(Utils.tail(keys), Utils.tail(children));
    }

    @Deprecated
    public Node head() {
        return createNewNode(Utils.head(keys), Utils.head(children));
    }

    public boolean shouldSplit() {
        return !satisfiesMaxChildren();
    }

    public abstract Node merge(Node otherNode);

    public abstract List<Node> stealFirstKeyFrom(Node rightSibling);
    public abstract List<Node> giveLastKeyTo(Node rightSibling);

    protected abstract Node createNewNode(List<String> keys, List<Node> children);

    public abstract Node insert(String key, String value);

    public abstract Node delete(String key);

    public abstract String smallestKey();

    public abstract String search(String key);
}