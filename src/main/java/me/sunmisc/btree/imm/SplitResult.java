package me.sunmisc.btree.imm;

import java.util.ArrayList;
import java.util.List;

public class SplitResult extends Node {
    public final String medianKey;
    public final Node leftNode;
    public final Node rightNode;

    public SplitResult(List<Object> splitData) {
        super(0, new ArrayList<>(), new ArrayList<>());
        this.medianKey = (String) splitData.get(0);
        this.leftNode = (Node) splitData.get(1);
        this.rightNode = (Node) splitData.get(2);
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
        throw new UnsupportedOperationException("Cannot merge SplitResult");
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
    @Override
    public List<Node> stealFirstKeyFrom(Node rightSibling) {
        return List.of();
    }

    @Override
    public List<Node> giveLastKeyTo(Node rightSibling) {
        return List.of();
    }
}