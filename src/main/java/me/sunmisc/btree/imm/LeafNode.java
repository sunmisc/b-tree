package me.sunmisc.btree.imm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static me.sunmisc.btree.imm.Constants.*;

public class LeafNode extends Node {
    // private final LearnedModel model;

    public LeafNode(int order, List<String> keys) {
        super(order, keys, List.of());

        // this.model = LearnedModel.retrain(keys);
    }
    protected Node forCreate(int order, List<String> keys) {
       // System.out.println(this.keys + " Found key \n" + keys+ " \n");
        return new LeafNode(order, keys);
    }
    protected LeafNode forMerge(int order, List<String> keys) {
        // System.out.println(this.keys + " STEAL \n" + keys+ " \n");
        return new LeafNode(order, keys);
    }

    protected LeafNode forSteal(int order, List<String> keys) {
     //   System.out.println(this.keys + " SPLIT \n" + keys+ " \n");
        return new LeafNode(order, keys);
    }


    protected Node forDelete(int order, List<String> keys) {
       // System.out.println(this.keys + " Found key \n" + keys+ " \n");
        return new LeafNode(order, keys);
    }

    public Node tail() {
        return forCreate(order, Utils.tail(keys));
    }

    @Override
    protected Node createNewNode(int order, List<String> keys, List<Node> children) {
        return new LeafNode(order, keys);
    }

    @Override
    public Node delete(boolean[] didChange, String key) {
        int idx = Collections.binarySearch(keys, key);
        if (idx < 0) {
            throw new IllegalArgumentException();
        }
        Utils.setRef(didChange);
        List<String> newKeys = Utils.withoutIdx(idx, keys);
        return forDelete(order, newKeys);
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

        return forMerge(order, newKeys);
    }


    @Override
    public Node insert(boolean[] didChange, String key, String value) {
        int idx = Collections.binarySearch(keys, key);

        List<String> newKeys;
        if (idx < 0) {
            idx = -idx - 1;
            newKeys = Utils.append(idx, key, keys);
        } else {
            newKeys = Utils.set(idx, key, keys);
        }
        Utils.setRef(didChange);
        LeafNode newLeaf = (LeafNode) forSteal(order, newKeys);
        return newLeaf.shouldSplit() ? new SplitResult(newLeaf.split()) : newLeaf;
    }

    public List<Object> split() {
        int cutoff = keys.size() >>> 1;
        String mid = keys.get(cutoff);

        List<List<String>> keyPair = Utils.splitAt(cutoff, keys);
        List<String> thisKeys = keyPair.get(0);
        List<String> otherKeys = keyPair.get(1);

        Node other = forSteal(order, otherKeys);
        Node thisSplit = forSteal(order, thisKeys);
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
        List<String> newRightKeys = rightSibling.keys.subList(1, rightSibling.keys.size()); // убираем первый ключ

        return List.of(
                forSteal(order, newKeys),
                new LeafNode(order, newRightKeys) // явно создаем новый rightSibling без первого ключа
        );
    }

    @Override
    public List<Node> giveLastKeyTo(Node rightSibling) {
        String keyToGive = keys.get(keys.size() - 1);

        List<String> newSiblingKeys = Utils.unshift(keyToGive, rightSibling.keys);
        List<String> newThisKeys = keys.subList(0, keys.size() - 1); // убираем последний ключ

        LeafNode newSibling = forMerge(order, newSiblingKeys);
        LeafNode newThis = new LeafNode(order, newThisKeys);

        return List.of(newThis, newSibling);
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