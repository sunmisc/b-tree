package me.sunmisc.btree.imm;

import java.util.ArrayList;
import java.util.List;

class LeafValueNode extends Node {
    private final String value;

    public LeafValueNode(String value) {
        super(0, new ArrayList<>(), new ArrayList<>());
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int getMinChildren() {
        return 0;
    }

    @Override
    public int getMaxChildren() {
        return 0;
    }

    @Override
    public Node merge(Node otherNode) {
        throw new UnsupportedOperationException("Cannot merge LeafValueNode");
    }

    @Override
    public List<Node> stealFirstKeyFrom(Node rightSibling) {
        return List.of();
    }

    @Override
    public List<Node> giveLastKeyTo(Node rightSibling) {
        return List.of();
    }

    @Override
    protected Node createNewNode(int order, List<String> keys, List<Node> children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node insert(boolean[] didChange, String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node delete(boolean[] didChange, String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String smallestKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String search(String key) {
        throw new UnsupportedOperationException();
    }
}
