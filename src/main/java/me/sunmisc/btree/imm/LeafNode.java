package me.sunmisc.btree.imm;

import me.sunmisc.btree.LearnedModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.sunmisc.btree.imm.Constants.*;

public class LeafNode extends Node {
    private final LearnedModel model;
    private final Map<Long, String> map;

    public LeafNode(int order, List<Long> keys) {
        super(order, keys, List.of());
        if (!LearnedModel.learned) {
            this.map = keys.stream().collect(
                    Collectors.toUnmodifiableMap(e -> e, e -> "" + e)
            );
            model = LearnedModel.retrain(List.of());
        } else {
            this.model = LearnedModel.retrain(keys);
            this.map = null;
        }
    }

    @Override
    protected Node createNewNode(int order, List<Long> keys, List<Node> children) {
        return new LeafNode(order, keys);
    }

    @Override
    public Node delete(boolean[] didChange, Long key) {
        int idx = model.search(keys, key);
        if (idx < 0) {
            throw new IllegalArgumentException();
        }
        Utils.setRef(didChange);
        List<Long> newKeys = Utils.withoutIdx(idx, keys);
        return new LeafNode(order, newKeys);
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
        List<Long> newKeys = new ArrayList<>(keys);
        newKeys.addAll(otherLeaf.keys);
        return new LeafNode(order, newKeys);
    }


    @Override
    public Node insert(boolean[] didChange, Long key, String value) {
        int idx = model.search(keys, key);

        List<Long> newKeys;
        if (idx < 0) {
            idx = -idx - 1;
            newKeys = Utils.append(idx, key, keys);
        } else {
            newKeys = Utils.set(idx, key, keys);
        }

        Utils.setRef(didChange);
        LeafNode newLeaf = new LeafNode(order, newKeys);
        return newLeaf.shouldSplit() ? new SplitResult(newLeaf.split()) : newLeaf;
    }

    public List<Object> split() {
        int cutoff = keys.size() >>> 1;
        Long mid = keys.get(cutoff);

        List<List<Long>> keyPair = Utils.splitAt(cutoff, keys);
        List<Long> thisKeys = keyPair.get(0);
        List<Long> otherKeys = keyPair.get(1);

        LeafNode other = new LeafNode(order, otherKeys);
        LeafNode thisSplit = new LeafNode(order, thisKeys);
        return List.of(mid, thisSplit, other);
    }

    @Override
    public int getSize() {
        return keys.size();
    }

    @Override
    public Long smallestKey() {
        return keys.getFirst();
    }

    @Override
    public List<Node> stealFirstKeyFrom(Node rightSibling) {
        Long stolenKey = rightSibling.keys.get(0);

        List<Long> newKeys = Utils.append(keys.size(), stolenKey, keys);

        List<Long> newSiblingKeys = Utils.tail(rightSibling.keys);

        return List.of(
                new LeafNode(order, newKeys),
                new LeafNode(rightSibling.order, newSiblingKeys)
        );
    }

    @Override
    public List<Node> giveLastKeyTo(Node rightSibling) {
        Long keyToGive = keys.get(keys.size() - 1);

        List<Long> newSiblingKeys = Utils.unshift(keyToGive, rightSibling.keys);

        LeafNode newSibling = new LeafNode(order, newSiblingKeys);
        LeafNode thisWithoutLastKey = (LeafNode) this.init();
        return List.of(thisWithoutLastKey, newSibling);
    }


    @Override
    public String search(Long key) {
        if (map != null) {
            return map.get(key);
        } else {
            int idx = model.searchEq(keys, key);
            if (idx < 0) {
                return null;
            } else {
                return "" + keys.get(idx);
            }
        }
    }
}