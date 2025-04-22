package me.sunmisc.btree.imm;

import java.util.ArrayList;
import java.util.List;

import static me.sunmisc.btree.imm.Constants.*;

public class Leaf extends Node {
    public Leaf(int order, List<String> keys) {
        super(order, keys, new ArrayList<>());
    }

    @Override
    protected Node createNewNode(int order, List<String> keys, List<Node> children) {
        return new Leaf(order, keys);
    }

    @Override
    public Node delete(boolean[] didChange, String key) {
        int idx = Utils.binSearch(keys, key);
        if (idx < 0) {
            return this;
        }

        Utils.setRef(didChange);
        List<String> newKeys = Utils.withoutIdx(idx, keys);
        return new Leaf(order, newKeys);
    }

    @Override
    public int getMinChildren() {
        return LEAF_MIN_CHILDREN;
    }

    @Override
    public int getMaxChildren() {
        return LEAF_MAX_CHILDREN;
    }

    @Override
    public Node merge(Node otherNode) {
        if (!(otherNode instanceof Leaf otherLeaf)) {
            throw new IllegalArgumentException("Can only merge with another Leaf node");
        }
        List<String> newKeys = new ArrayList<>(keys);
        newKeys.addAll(otherLeaf.keys);
        return new Leaf(order, newKeys);
    }

    public int idxForKey(String key) {
        return Utils.binSearch(keys, key);
    }

    @Override
    public Node insert(boolean[] didChange, String key, String value) {
        int idx = idxForKey(key);

        List<String> newKeys;
        if (idx < 0) {
            idx = -idx - 1;
            newKeys = Utils.append(idx, key, keys);
        } else {
            newKeys = Utils.set(idx, key, keys);
        }

        Utils.setRef(didChange);
        Leaf newLeaf = new Leaf(order, newKeys);
        return newLeaf.shouldSplit() ? new SplitResult(newLeaf.split()) : newLeaf;
    }

    public List<Object> split() {
        int cutoff = keys.size() >>> 1;
        String mid = keys.get(cutoff);

        List<List<String>> keyPair = Utils.splitAt(cutoff, keys);
        List<String> thisKeys = keyPair.get(0);
        List<String> otherKeys = keyPair.get(1);

        Leaf other = new Leaf(order, otherKeys);
        Leaf thisSplit = new Leaf(order, thisKeys);
        return List.of(mid, thisSplit, other);
    }

    @Override
    public int getSize() {
        return keys.size();
    }

    @Override
    public String smallestKey() {
        return keys.getFirst();
    }

    @Override
    public List<Node> stealFirstKeyFrom(Node rightSibling) {
        String stolenKey = rightSibling.keys.get(0);

        List<String> newKeys = Utils.append(keys.size(), stolenKey, keys);

        List<String> newSiblingKeys = Utils.tail(rightSibling.keys);

        return List.of(
                new Leaf(order, newKeys),
                new Leaf(rightSibling.order, newSiblingKeys)
        );
    }

    @Override
    public List<Node> giveLastKeyTo(Node rightSibling) {
        String keyToGive = keys.get(keys.size() - 1);

        List<String> newSiblingKeys = Utils.unshift(keyToGive, rightSibling.keys);

        Leaf newSibling = new Leaf(order, newSiblingKeys);
        Leaf thisWithoutLastKey = (Leaf) this.init();
        return List.of(thisWithoutLastKey, newSibling);
    }


    @Override
    public String search(String key) {
        int idx = Utils.binSearch(keys, key);
        if (idx < 0) {
            return null;
        }
        return keys.get(idx);
    }
}