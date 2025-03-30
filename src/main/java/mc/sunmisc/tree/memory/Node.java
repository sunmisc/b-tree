package mc.sunmisc.tree.memory;

import java.util.Optional;
import java.util.stream.Stream;

public interface Node<K extends Comparable<K>, V>
        extends Iterable<Node<K,V>>, Comparable<Node<K,V>> {

    Split<K,V> tryPushOrSplit(K key, V value);

    Optional<V> search(K key);

    Stream<K> keys();

    Stream<V> between(K fromKey, K toKey);

    record Split<K extends Comparable<K>,V>(K mid, Node<K,V> left, Node<K,V> right) {}
}
