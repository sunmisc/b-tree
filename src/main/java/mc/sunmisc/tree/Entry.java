package mc.sunmisc.tree;

import java.io.Serializable;

public record Entry<K,V>(K key, V value) implements Serializable {

    @java.io.Serial
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();
    }

    @java.io.Serial
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
    }
}
