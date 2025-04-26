package me.sunmisc.btree.imm;

import me.sunmisc.btree.LearnedModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InternalNode extends Node {

    public static final String REPLACE = "REPLACE";
    public static final String STEAL_KEY_FROM_LEFT = "STEAL_KEY_FROM_LEFT";
    public static final String STEAL_KEY_FROM_RIGHT = "STEAL_KEY_FROM_RIGHT";
    public static final String MERGE = "MERGE";

    public InternalNode(int order, List<String> keys, List<Node> children) {
        super(order, keys, children);
    }


    @Override
    public int getMinChildren() {
        return Constants.INTERNAL_MIN_CHILDREN;
    }

    @Override
    public int getMaxChildren() {
        return Constants.INTERNAL_MAX_CHILDREN;
    }

    @Override
    protected Node createNewNode(int order, List<String> keys, List<Node> children) {
        return new InternalNode(order, keys, children);
    }

    @Override
    public Node merge(Node otherNode) {
        List<String> toConcat = Utils.unshift(otherNode.smallestKey(), otherNode.keys);
        List<String> newKeys = new ArrayList<>(keys);
        newKeys.addAll(toConcat);
        List<Node> newChildren = new ArrayList<>(children);
        newChildren.addAll(otherNode.children);
        return new InternalNode(order, newKeys, newChildren);
    }

    public DeletionStrategy chooseComplexDeletionStrategy(int childIdx, Node child) {
        if (child.satisfiesMinChildren()) {
            return new DeletionStrategy(REPLACE);
        }
        boolean hasRightSibling = childIdx + 1 < children.size();
        boolean hasLeftSibling = childIdx - 1 >= 0;
        Node node = null;
        if (hasLeftSibling || hasRightSibling) {
            node = new EmptyNode();
        }
        Node right = hasRightSibling
                ? children.get(childIdx + 1)
                : node;
        Node left = hasLeftSibling
                ? children.get(childIdx - 1)
                : node;
        int minChildren = child.getMinChildren();

        String strategy;
        if (right.getSize() >= left.getSize()) {
            strategy = right.getSize() <= minChildren ? MERGE : STEAL_KEY_FROM_RIGHT;
            return new DeletionStrategy(strategy, child, right, childIdx);
        } else {
            strategy = left.getSize() <= minChildren ? MERGE : STEAL_KEY_FROM_LEFT;
            return new DeletionStrategy(strategy, left, child, childIdx - 1);
        }
    }

    @Override
    public Node delete(boolean[] didChange, String key) {
        int rawIndex = Collections.binarySearch(keys, key);
        int index = rawIndex >= 0 ? rawIndex + 1 : -rawIndex - 1;

        Node origChild = children.get(index);
        Node child = origChild.delete(didChange, key);

        DeletionStrategy strategyInfo = chooseComplexDeletionStrategy(index, child);
        String strategy = strategyInfo.strategy;

        return switch (strategy) {
            case REPLACE -> createNewNode(order,
                    keys,
                    withReplacedChildren(index, List.of(child))
            );
            case MERGE -> {
                Node leftNode = strategyInfo.leftNode;
                Node rightNode = strategyInfo.rightNode;
                int leftNodeIdx = strategyInfo.leftNodeIdx;

                yield withMergedChildren(leftNodeIdx, leftNode, rightNode);
            }
            default -> {
                Node leftNode = strategyInfo.leftNode;
                Node rightNode = strategyInfo.rightNode;
                int leftNodeIdx = strategyInfo.leftNodeIdx;

                List<Node> newNodes = strategy.equals(STEAL_KEY_FROM_RIGHT)
                        ? leftNode.stealFirstKeyFrom(rightNode)
                        : leftNode.giveLastKeyTo(rightNode);
                Node newLeftNode = newNodes.get(0);
                Node newRightNode = newNodes.get(1);

                List<Node> withReplacedChildren = withReplacedChildren(
                        leftNodeIdx,
                        List.of(newLeftNode, newRightNode));
                String newKey = newRightNode.smallestKey();
                yield createNewNode(
                        order,
                        Utils.set(leftNodeIdx, newKey, keys),
                        withReplacedChildren
                );
            }
        };
    }

    public InternalNode withMergedChildren(int leftChildIdx, Node leftNode, Node rightNode) {
        Node mergedChild = leftNode.merge(rightNode);
        List<String> newKeys = Utils.withoutIdx(leftChildIdx, keys);

        boolean areLeftmostNodes = leftChildIdx == 0;
        if (!areLeftmostNodes) {
            newKeys = Utils.set(leftChildIdx - 1, mergedChild.smallestKey(), newKeys);
        }

        List<Node> newChildren = new ArrayList<>(children);
        newChildren.remove(leftChildIdx);
        newChildren.set(leftChildIdx, mergedChild);

        return new InternalNode(order, newKeys, newChildren);
    }


    public List<Node> stealFirstKeyFrom(Node rightSibling) {
        List<String> newKeys = new ArrayList<>(keys);
        newKeys.add(rightSibling.smallestKey());
        List<Node> newChildren = new ArrayList<>(children);
        newChildren.add(rightSibling.children.get(0));
        return List.of(new InternalNode(order, newKeys, newChildren), rightSibling.tail());
    }

    public List<Node> giveLastKeyTo(Node rightSibling) {
        Node stolenValue = children.getLast();
        List<String> newSiblingKeys = Utils.unshift(rightSibling.smallestKey(), rightSibling.keys);
        List<Node> newSiblingChildren = Utils.unshift(stolenValue, rightSibling.children);
        InternalNode newSibling = new InternalNode(order, newSiblingKeys, newSiblingChildren);
        return List.of(this.init(), newSibling);
    }

    public List<Node> withReplacedChildren(int idx, List<Node> newChildren) {
        List<Node> replaced = new ArrayList<>(children);
        for (int i = 0; i < newChildren.size(); i++) {
            replaced.set(idx + i, newChildren.get(i));
        }
        return replaced;
    }

    @Override
    public String smallestKey() {
        return children.getFirst().smallestKey();
    }

    private List<Object> split() {
        int mid = keys.size() >>> 1;
        String midVal = keys.get(mid);
        InternalNode left = new InternalNode(order,
                this.keys.subList(0, mid),
                this.children.subList(0, mid + 1));
        InternalNode right = new InternalNode(order,
                this.keys.subList(mid + 1, this.keys.size()),
                this.children.subList(mid + 1, this.children.size())
        );
        return List.of(midVal, left, right);
    }


    public InternalNode withSplitChild(String newKey, Node splitChild, Node newChild) {
        int childIdx = Collections.binarySearch(keys, newKey);
        int index = childIdx < 0 ? -childIdx - 1 : childIdx;

        List<String> newKeys = Utils.append(index, newKey, keys);
        List<Node> newChildren = Utils.append(index + 1, newChild, children);
        newChildren.set(index, splitChild);
        return new InternalNode(order, newKeys, newChildren);
    }

    @Override
    public Node insert(boolean[] didChange, String key, String value) {
        int childIdx = Collections.binarySearch(keys, key);
        int index = childIdx < 0 ? -childIdx - 1 : childIdx + 1;

        Node child = children.get(index);
        Node newChild = child.insert(didChange, key, value);

        if (newChild instanceof SplitResult splitArr) {
            String medianKey = splitArr.medianKey;
            Node splitChild = splitArr.leftNode;
            Node _newChild = splitArr.rightNode;

            InternalNode withSplitChild = withSplitChild(medianKey, splitChild, _newChild);
            return withSplitChild.shouldSplit() ?
                    new SplitResult(withSplitChild.split()) : withSplitChild;
        }

        return createNewNode(order, keys, withReplacedChildren(index, List.of(newChild)));
    }

    @Override
    public String search(String key) {
        int index = Collections.binarySearch(keys, key);
        index = index >= 0 ? index + 1 : -index - 1;
        return children.get(index).search(key);
    }
}