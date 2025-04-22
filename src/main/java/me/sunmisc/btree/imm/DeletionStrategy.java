package me.sunmisc.btree.imm;

public class DeletionStrategy {
    public final String strategy;
    public final Node leftNode;
    public final Node rightNode;
    public final int leftNodeIdx;

    public DeletionStrategy(String strategy) {
        this(strategy, null, null, 0);
    }

    public DeletionStrategy(String strategy, Node leftNode, Node rightNode, int leftNodeIdx) {
        this.strategy = strategy;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.leftNodeIdx = leftNodeIdx;
    }

    public enum Strategy {
        REPLACE,
        STEAL_KEY_FROM_LEFT,
        STEAL_KEY_FROM_RIGHT,
        MERGE
    }
}