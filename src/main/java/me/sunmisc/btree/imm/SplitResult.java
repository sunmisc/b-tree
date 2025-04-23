package me.sunmisc.btree.imm;

import java.util.ArrayList;
import java.util.List;

public class SplitResult extends Node {
    public final Long medianKey;
    public final Node leftNode;
    public final Node rightNode;

    public SplitResult(List<Object> splitData) {
        super(0, new ArrayList<>(), new ArrayList<>());
        this.medianKey = (Long) splitData.get(0);
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
    protected Node createNewNode(int order, List<Long> keys, List<Node> children) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node insert(boolean[] didChange, Long key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node delete(boolean[] didChange, Long key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long smallestKey() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String search(Long key) {
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