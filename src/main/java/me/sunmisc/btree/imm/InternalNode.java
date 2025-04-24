package me.sunmisc.btree.imm;

import me.sunmisc.btree.LearnedModel;

import java.util.ArrayList;
import java.util.List;

public class InternalNode extends Node {

    public static final String REPLACE = "REPLACE";
    public static final String STEAL_KEY_FROM_LEFT = "STEAL_KEY_FROM_LEFT";
    public static final String STEAL_KEY_FROM_RIGHT = "STEAL_KEY_FROM_RIGHT";
    public static final String MERGE = "MERGE";

    public InternalNode(int order, List<Long> keys,
                        List<Node> children) {
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
    protected Node createNewNode(int order, List<Long> keys, List<Node> children) {
        return new InternalNode(order, keys, children);
    }

    @Override
    public Node merge(Node otherNode) {
        List<Long> toConcat = Utils.unshift(otherNode.smallestKey(), otherNode.keys);
        List<Long> newKeys = new ArrayList<>(keys);
        newKeys.addAll(toConcat);
        List<Node> newChildren = new ArrayList<>(children);
        newChildren.addAll(otherNode.children);
        return new InternalNode(order, newKeys, newChildren);
    }

    public DeletionStrategy chooseComplexDeletionStrategy(int childIdx, Node newChild) {
        if (newChild.satisfiesMinChildren()) {
            return new DeletionStrategy(REPLACE);
        }

        boolean hasRightSibling = childIdx + 1 < children.size();
        boolean hasLeftSibling = childIdx - 1 >= 0;

        boolean isLeaf = newChild instanceof LeafNode;

        Node nullSibling = new EmptyNode();
        Node rightSibling = hasRightSibling ? children.get(childIdx + 1) : nullSibling;
        Node leftSibling = hasLeftSibling ? children.get(childIdx - 1) : nullSibling;

        int minChildren = isLeaf ? Constants.LEAF_MIN_CHILDREN : Constants.INTERNAL_MIN_CHILDREN;

        String strategy;
        if (rightSibling.getSize() >= leftSibling.getSize()) {
            if (rightSibling.getSize() <= minChildren) {
                strategy = MERGE;
            } else {
                strategy = STEAL_KEY_FROM_RIGHT;
            }
            return new DeletionStrategy(strategy, newChild, rightSibling, childIdx);
        } else {
            if (leftSibling.getSize() <= minChildren) {
                strategy = MERGE;
            } else {
                strategy = STEAL_KEY_FROM_LEFT;
            }
            return new DeletionStrategy(strategy, leftSibling, newChild, childIdx - 1);
        }
    }

    @Override
    public Node delete(boolean[] didChange, Long key) {
        int childIdx = LearnedModel.binSearch(keys, key);
        int index;
        if (childIdx >= 0) {
            // Если ключ найден в текущем узле, идём в правый дочерний узел
            index = childIdx + 1;
        } else {
            // Если ключ не найден, используем точку вставки
            index = -childIdx - 1;
        }

        Node origChild = children.get(index);
        Node child = origChild.delete(didChange, key);

        if (!Utils.isSet(didChange)) {
            return this;
        }

        DeletionStrategy strategyInfo = chooseComplexDeletionStrategy(index, child);
        String strategy = strategyInfo.strategy;
        if (strategy.equals(REPLACE)) {
            return withReplacedChildren(index, List.of(child));
        }

        Node leftNode = strategyInfo.leftNode;
        Node rightNode = strategyInfo.rightNode;
        int leftNodeIdx = strategyInfo.leftNodeIdx;

        if (strategy.equals(MERGE)) {
            return withMergedChildren(leftNodeIdx, leftNode, rightNode);
        }

        Node newLeftNode;
        Node newRightNode;
        if (strategy.equals(STEAL_KEY_FROM_RIGHT)) {
            List<Node> newNodes = leftNode.stealFirstKeyFrom(rightNode);
            newLeftNode = newNodes.get(0);
            newRightNode = newNodes.get(1);
        } else {
            List<Node> newNodes = leftNode.giveLastKeyTo(rightNode);
            newLeftNode = newNodes.get(0);
            newRightNode = newNodes.get(1);
        }

        InternalNode withReplacedChildren = withReplacedChildren(leftNodeIdx, List.of(newLeftNode, newRightNode));
        int keyIdxToReplace = leftNodeIdx;
        Long newKey = newRightNode.smallestKey();
        withReplacedChildren.keys = Utils.set(keyIdxToReplace, newKey, withReplacedChildren.keys);
        return withReplacedChildren;
    }

    public InternalNode withMergedChildren(int leftChildIdx, Node leftNode, Node rightNode) {
        Node mergedChild = leftNode.merge(rightNode);
        List<Long> newKeys = Utils.withoutIdx(leftChildIdx, keys);

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
        List<Long> newKeys = new ArrayList<>(keys);
        newKeys.add(rightSibling.smallestKey());
        List<Node> newChildren = new ArrayList<>(children);
        newChildren.add(rightSibling.children.get(0));
        return List.of(new InternalNode(order, newKeys, newChildren), rightSibling.tail());
    }

    public List<Node> giveLastKeyTo(Node rightSibling) {
        Node stolenValue = children.getLast();
        List<Long> newSiblingKeys = Utils.unshift(rightSibling.smallestKey(), rightSibling.keys);
        List<Node> newSiblingChildren = Utils.unshift(stolenValue, rightSibling.children);
        InternalNode newSibling = new InternalNode(order, newSiblingKeys, newSiblingChildren);
        return List.of(this.init(), newSibling);
    }

    public InternalNode withReplacedChildren(int idx, List<Node> newChildren) {
        List<Node> replaced = new ArrayList<>(children);
        for (int i = 0; i < newChildren.size(); i++) {
            replaced.set(idx + i, newChildren.get(i));
        }
        return new InternalNode(order, keys, replaced);
    }

    @Override
    public Long smallestKey() {
        return children.getFirst().smallestKey();
    }

    private List<Object> split() {
        int mid = keys.size() >>> 1;
        Long midVal = keys.get(mid);
        InternalNode left = new InternalNode(order,
                this.keys.subList(0, mid),
                this.children.subList(0, mid + 1));
        InternalNode right = new InternalNode(order,
                this.keys.subList(mid + 1, this.keys.size()),
                this.children.subList(mid + 1, this.children.size())
        );
        return List.of(midVal, left, right);
    }


    public InternalNode withSplitChild(Long newKey, Node splitChild, Node newChild) {
        int childIdx = LearnedModel.binSearch(keys, newKey);
        int index = childIdx < 0 ? -childIdx - 1 : childIdx;

        List<Long> newKeys = Utils.append(index, newKey, keys);
        List<Node> newChildren = Utils.append(index + 1, newChild, children);
        newChildren.set(index, splitChild);
        return new InternalNode(order, newKeys, newChildren);
    }

    @Override
    public Node insert(boolean[] didChange, Long key, String value) {
        int childIdx = LearnedModel.binSearch(keys, key);
        int index = childIdx < 0 ? -childIdx - 1 : childIdx;

        Node child = children.get(index);
        Node newChild = child.insert(didChange, key, value);

        if (!Utils.isSet(didChange)) {
            return this;
        }

        if (newChild instanceof SplitResult splitArr) {
            Long medianKey = splitArr.medianKey;
            Node splitChild = splitArr.leftNode;
            Node _newChild = splitArr.rightNode;

            InternalNode withSplitChild = withSplitChild(medianKey, splitChild, _newChild);
            return withSplitChild.shouldSplit() ?
                    new SplitResult(withSplitChild.split()) : withSplitChild;
        }

        return withReplacedChildren(index, List.of(newChild));
    }

    @Override
    public String search(Long key) {
        int index = LearnedModel.binSearch(keys, key);
        index = index >= 0 ? index + 1 : -index - 1;

        return children.get(index).search(key);
    }
}