package me.sunmisc.btree.imm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class InternalNode extends Node {

    public InternalNode(List<String> keys, List<Node> children) {
        super(keys, children);
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
    protected Node createNewNode(List<String> keys, List<Node> children) {
        return new InternalNode(keys, children);
    }

    @Override
    public Node merge(Node otherNode) {
        List<String> toConcat = Utils.unshift(otherNode.smallestKey(), otherNode.keys);
        List<String> newKeys = new ArrayList<>(keys);
        newKeys.addAll(toConcat);
        List<Node> newChildren = new ArrayList<>(children);
        newChildren.addAll(otherNode.children);
        return new InternalNode(newKeys, newChildren);
    }

    private Node rebalanceNode(int childIdx, Node child) {
        if (child.satisfiesMinChildren()) {
            return createNewNode(keys, withReplacedChildren(childIdx, List.of(child)));
        }
        boolean hasRightSibling = childIdx + 1 < children.size();
        boolean hasLeftSibling = childIdx > 0;
        // todo:
        Node right = hasRightSibling ? children.get(childIdx + 1) : null;
        Node left = hasLeftSibling ? children.get(childIdx - 1) : null;
        int minChildren = child.getMinChildren();

        if (hasRightSibling && (!hasLeftSibling || right.getSize() >= left.getSize())) {
            if (right.getSize() <= minChildren) {
                return withMergedChildren(childIdx, child, right);
            }
            List<Node> newNodes = child.stealFirstKeyFrom(right);
            return createUpdatedNode(childIdx, newNodes);
        } else if (hasLeftSibling) {
            if (left.getSize() <= minChildren) {
                return withMergedChildren(childIdx - 1, left, child);
            }
            List<Node> newNodes = left.giveLastKeyTo(child);
            return createUpdatedNode(childIdx - 1, newNodes);
        }
        return createNewNode(keys, withReplacedChildren(childIdx, List.of(child)));
    }

    private Node createUpdatedNode(int keyIdx, List<Node> newNodes) {
        Node left = newNodes.getFirst();
        Node right = newNodes.getLast();
        return createNewNode(
                Utils.set(keyIdx, right.smallestKey(), keys),
                withReplacedChildren(keyIdx, List.of(left, right))
        );
    }

    @Override
    public Node delete(String key) {
        int rawIndex = Collections.binarySearch(keys, key);
        int index = rawIndex >= 0 ? rawIndex + 1 : -rawIndex - 1;
        Node origChild = children.get(index);
        Node child = origChild.delete(key);
        return rebalanceNode(index, child);
    }

    private InternalNode withMergedChildren(int leftChildIdx, Node leftNode, Node rightNode) {
        Node mergedChild = leftNode.merge(rightNode);
        List<String> newKeys = Utils.withoutIdx(leftChildIdx, keys);
        if (leftChildIdx > 0) {
            newKeys = Utils.set(leftChildIdx - 1, mergedChild.smallestKey(), newKeys);
        }
        List<Node> newChildren = new ArrayList<>(children);
        newChildren.remove(leftChildIdx);
        newChildren.set(leftChildIdx, mergedChild);

        return new InternalNode(newKeys, newChildren);
    }

    @Override
    public List<Node> stealFirstKeyFrom(Node rightSibling) {
        List<String> newKeys = new ArrayList<>(keys);
        newKeys.add(rightSibling.smallestKey());
        List<Node> newChildren = new ArrayList<>(children);
        newChildren.add(rightSibling.children.getFirst());
        return List.of(new InternalNode(newKeys, newChildren), rightSibling.tail());
    }
    @Override
    public List<Node> giveLastKeyTo(Node rightSibling) {
        Node stolenValue = children.getLast();
        List<String> newSiblingKeys = Utils.unshift(rightSibling.smallestKey(), rightSibling.keys);
        List<Node> newSiblingChildren = Utils.unshift(stolenValue, rightSibling.children);
        InternalNode newSibling = new InternalNode(newSiblingKeys, newSiblingChildren);
        return List.of(this.head(), newSibling);
    }

    // todo: optimize
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
        InternalNode left = new InternalNode(
                this.keys.subList(0, mid),
                this.children.subList(0, mid + 1));
        InternalNode right = new InternalNode(
                this.keys.subList(mid + 1, this.keys.size()),
                this.children.subList(mid + 1, this.children.size())
        );
        return List.of(midVal, left, right);
    }

    private InternalNode withSplitChild(String newKey, Node splitChild, Node newChild) {
        int childIdx = Collections.binarySearch(keys, newKey);
        int index = childIdx < 0 ? -childIdx - 1 : childIdx;

        List<String> newKeys = Utils.append(index, newKey, keys);
        List<Node> newChildren = Utils.append(index + 1, newChild, children);
        newChildren.set(index, splitChild);
        return new InternalNode(newKeys, newChildren);
    }

    @Override
    public Node insert(String key, String value) {
        int childIdx = Collections.binarySearch(keys, key);
        int index = childIdx < 0 ? -childIdx - 1 : childIdx + 1;

        Node child = children.get(index);
        Node newChild = child.insert(key, value);

        if (newChild instanceof SplitResult splitArr) {
            String medianKey = splitArr.medianKey;
            Node splitChild = splitArr.leftNode;
            Node _newChild = splitArr.rightNode;

            InternalNode withSplitChild = withSplitChild(medianKey, splitChild, _newChild);
            return withSplitChild.shouldSplit()
                    ? new SplitResult(withSplitChild.split())
                    : withSplitChild;
        }

        return createNewNode(keys, withReplacedChildren(index, List.of(newChild)));
    }

    @Override
    public String search(String key) {
        int index = Collections.binarySearch(keys, key);
        index = index >= 0 ? index + 1 : -index - 1;
        return children.get(index).search(key);
    }
}