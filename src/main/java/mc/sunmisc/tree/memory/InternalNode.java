package mc.sunmisc.tree.memory;

import java.util.*;
import java.util.stream.Stream;

import static mc.sunmisc.tree.io.BPlusTreeIo.THRESHOLD;

public final class InternalNode<K extends Comparable<K>, V> implements Node<K, V> {
    private final List<K> keys;
    private final List<Node<K, V>> children;

    public InternalNode() {
        this(List.of(), List.of());
    }

    public InternalNode(List<K> keys, List<Node<K,V>> children) {
        this.keys = new ArrayList<>(keys);
        this.children = new ArrayList<>(children);
    }

    @Override
    public Split<K, V> tryPushOrSplit(K key, V value) {
        final int idx = Collections.binarySearch(keys, key);
        final int index = idx < 0 ? -idx - 1 : idx;
        final Split<K, V> split = children.get(index).tryPushOrSplit(key, value);

        if (split == null) {
            return null;
        }
        // children.set(index, split.left());
        children.add(index + 1, split.right());
        keys.add(index, split.mid());
        if (keys.size() >= THRESHOLD) {
            return split();
        }
        return null;
    }
    private Split<K, V> split() {
        final int mid = keys.size() >>> 1;
        K midKey = keys.get(mid);
        final InternalNode<K, V> newInternal = new InternalNode<>(
                keys.subList(mid + 1, keys.size()),
                children.subList(mid + 1, children.size())
        );
        keys.subList(mid, keys.size()).clear();
        children.subList(mid + 1, children.size()).clear();
        System.out.println(midKey + " < " + keys + " > " + newInternal.keys);
        return new Split<>(midKey, this, newInternal);
    }

    @Override
    public Optional<V> search(K key) {
        int index = Collections.binarySearch(keys, key);
        index = (index >= 0) ? index + 1 : -index - 1;
        return children.get(index).search(key);
    }

    @Override
    public Stream<V> between(K fromKey, K toKey) {
        int index = Collections.binarySearch(keys, fromKey);
        index = (index >= 0) ? index + 1 : -index - 1;
        return children.get(index).between(fromKey, toKey);
    }

    @Override
    public Stream<K> keys() {
        return keys.stream();
    }


    @Override
    public Iterator<Node<K, V>> iterator() {
        return children.iterator();
    }

    @Override
    public int compareTo(Node<K, V> o) {
        return keys.getFirst().compareTo(o.keys().findFirst().orElseThrow());
    }
}
