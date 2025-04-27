package me.sunmisc.btree.imm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.sunmisc.btree.imm.Constants.*;

public final class LeafNode extends Node {
    // private final LearnedModel model;

    public LeafNode(List<String> keys) {
        super(keys, List.of());
        // this.model = LearnedModel.retrain(keys);
    }

    private Node create(List<String> keys) {
        return new LeafNode(keys);
    }

    public Node tail() {
        return create(Utils.tail(keys));
    }
    public Node head() {
        return create(Utils.head(keys));
    }

    @Override
    protected Node createNewNode(List<String> keys, List<Node> children) {
        return new LeafNode(keys);
    }

    @Override
    public Node delete(String key) {
        int idx = Collections.binarySearch(keys, key);
        if (idx < 0) {
            throw new IllegalArgumentException();
        }

        List<String> newKeys = Utils.withoutIdx(idx, keys);
        return create(newKeys);
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
        if (!(otherNode instanceof LeafNode otherLeaf)) {
            throw new IllegalArgumentException("Can only merge with another Leaf node");
        }
        List<String> newKeys = new ArrayList<>(keys);
        newKeys.addAll(otherLeaf.keys);

        return create(newKeys);
    }


    @Override
    public Node insert(String key, String value) {
        int idx = Collections.binarySearch(keys, key);

        List<String> newKeys;
        if (idx < 0) {
            idx = -idx - 1;
            newKeys = Utils.append(idx, key, keys);
        } else {
            newKeys = Utils.set(idx, key, keys);
        }
        LeafNode newLeaf = (LeafNode) create(newKeys);
        return newLeaf.shouldSplit() ? new SplitResult(newLeaf.split()) : newLeaf;
    }

    public List<Object> split() {
        int cutoff = keys.size() >>> 1;
        String mid = keys.get(cutoff);

        List<List<String>> keyPair = Utils.splitAt(cutoff, keys);
        List<String> thisKeys = keyPair.get(0);
        List<String> otherKeys = keyPair.get(1);

        Node other = create(otherKeys);
        Node thisSplit = create(thisKeys);
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

        return List.of(
                create(newKeys),
                rightSibling.tail()
        );
    }

    @Override
    public List<Node> giveLastKeyTo(Node rightSibling) {
        String keyToGive = keys.get(keys.size() - 1);

        List<String> newSiblingKeys = Utils.unshift(keyToGive, rightSibling.keys);

        return List.of(head(), create(newSiblingKeys));
    }


    @Override
    public String search(String key) {
        int idx = Collections.binarySearch(keys, key);
        if (idx < 0) {
            return null;
        } else {
            return keys.get(idx);
        }
    }
}