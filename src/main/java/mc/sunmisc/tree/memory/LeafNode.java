package mc.sunmisc.tree.memory;

import java.util.*;
import java.util.stream.Stream;

import static mc.sunmisc.tree.io.BPlusTreeIo.THRESHOLD;

public final class LeafNode<K extends Comparable<K>, V> implements Node<K, V> {
    private final List<Map.Entry<K, V>> values;
    private LeafNode<K,V> next;

    public LeafNode() {
        this.values = new ArrayList<>();
    }

    public LeafNode(List<Map.Entry<K,V>> values) {
        this.values = new ArrayList<>(values);
    }

    @Override
    public Split<K, V> tryPushOrSplit(K key, V value) {
        final Map.Entry<K, V> e = Map.entry(key, value);
        final int index = binSearch(key);
        if (index >= 0) {
            values.set(index, e);
        } else {
            values.add(-index - 1, e);
        }
        if (values.size() < THRESHOLD) {
            return null;
        }
        final int mid = values.size() >>> 1;
        LeafNode<K, V> newLeaf = new LeafNode<>(values.subList(mid, values.size()));
        values.subList(mid, values.size()).clear();
        newLeaf.next = next;
        next = newLeaf;
        return new Split<>(newLeaf.values.getFirst().getKey(), this, newLeaf);
    }

    @Override
    public Optional<V> search(K key) {
        final int index = binSearch(key);
        return index < 0
                ? Optional.empty()
                : Optional.ofNullable(values.get(index)).map(e -> e.getValue());
    }

    @Override
    public Stream<V> between(K fromKey, K toKey) {
        final int index = binSearch(fromKey);
        return Stream.concat(
                values.subList(index, values.size()).stream(),
                next().flatMap(e -> e.values.stream())
        ).takeWhile(e -> e.getKey().compareTo(toKey) <= 0).map(Map.Entry::getValue);
    }

    @Override
    public Stream<K> keys() {
        return values.stream().map(Map.Entry::getKey);
    }

    private Stream<LeafNode<K,V>> next() {
        if (next == null) {
            return Stream.empty();
        }
        return Stream.iterate(next,
                idx -> idx.next != null,
                idx -> idx.next);
    }

    @Override
    public int compareTo(Node<K, V> o) {
        return values.getFirst().getKey().compareTo(o.keys().findFirst().orElseThrow());
    }

    @Override
    public Iterator<Node<K, V>> iterator() {
        return Collections.emptyIterator();
    }

    private int binSearch(K key) {
        int low = 0;
        int high = values.size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            K midVal = values.get(mid).getKey();
            int cmp = midVal.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }
}