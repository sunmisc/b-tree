package me.sunmisc.btree.imm;

import java.util.ArrayList;
import java.util.List;

public class EmptyNode extends Node {
    public EmptyNode() {
        super(0, new ArrayList<>(), new ArrayList<>());
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
        throw new UnsupportedOperationException("Cannot merge EmptyNode");
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
        return null;
    }

    @Override
    public String search(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Node> stealFirstKeyFrom(Node rightSibling) {
        return List.of();
    }

    @Override
    public List<Node> giveLastKeyTo(Node rightSibling) {
        return List.of();
    }
}