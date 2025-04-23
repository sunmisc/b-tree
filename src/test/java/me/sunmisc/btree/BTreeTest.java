package me.sunmisc.btree;

import me.sunmisc.btree.imm.BTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BTreeTest {

    @Test
    public void testAddAndPoll() {
        TreeMap<Long, String> map = new TreeMap<>();
        BTree bTree = new BTree();
        for (long i = 0; i < 10000; ++i) {
            map.put(i, i+"");
            bTree.insert(i, i+"");
        }
        for (long i = 0; i < 10000; ++i) {
            long r = i + 10000;
            bTree.insert(r, r+"");
            bTree.delete(bTree.getRoot().smallestKey());

            map.put(r, r+"");
            map.pollFirstEntry();
        }
        for (long i = 0; i < 10000; ++i) {
            if (!Objects.equals(map.get(i), bTree.search(i))) {
                throw new IllegalStateException();
            }
        }
    }

    @Test
    public void testAdd() {
        TreeMap<Long, String> map = new TreeMap<>();
        BTree bTree = new BTree();
        for (long i = 0; i < 10000; ++i) {
            map.put(i, i+"");
            bTree.insert(i, i+"");
        }
        for (long i = 0; i < 10000; ++i) {
            if (!Objects.equals(map.get(i), bTree.search(i))) {
                throw new IllegalStateException();
            }
        }
    }
    @ParameterizedTest
    @ValueSource(ints = {1, 3, 3, 5, 32, 63})
    public void testRemoveAndAddRand(int step) {
        TreeMap<Long, String> expectedMap = new TreeMap<>();
        BTree actualBTree = new BTree();
        int maxValue = 1000;

        // Act - Initial insertion
        for (int i = 0; i < maxValue; i++) {
            long key = ThreadLocalRandom.current().nextInt(1_000_000);
            expectedMap.put(key, key+"");
            actualBTree.insert(key, key+"");
        }

        actualBTree.print();
        // Act - Remove and add operations
        for (long i = 0; i < maxValue; i += step) {
            long keyToRemove = i;
            long keyToAdd = i + step;

            // Remove operation
            expectedMap.remove(keyToRemove);
            actualBTree.delete(keyToRemove);

            // Add operation
            expectedMap.put(keyToAdd, keyToAdd+"");
            actualBTree.insert(keyToAdd, keyToAdd+"");
        }
        // Assert
        for (long i = 0; i < maxValue; i++) {
            long key = i;
            String expected = expectedMap.get(key);
            String actual = actualBTree.search(key);
            if (!Objects.equals(expected, actual)) {
                actualBTree.print();
            }
            assertEquals(expected, actual, "Key " + key);
        }

    }
    @ParameterizedTest
    @ValueSource(ints = {1, 3, 3, 5, 32, 63})
    public void testRemoveAndAdd(int step) {
        TreeMap<Long, String> expectedMap = new TreeMap<>();
        BTree actualBTree = new BTree();
        int maxValue = 1000;

        // Act - Initial insertion
        for (long i = 0; i < maxValue; i++) {
            long key = i;
            expectedMap.put(key, key+"");
            actualBTree.insert(key, key+"");
        }

        // Act - Remove and add operations
        for (long i = 0; i < maxValue; i += step) {
            long keyToRemove = i;
            long keyToAdd = i + step;

            // Remove operation
            expectedMap.remove(keyToRemove);
            actualBTree.delete(keyToRemove);

            // Add operation
            expectedMap.put(keyToAdd, keyToAdd+"");
            actualBTree.insert(keyToAdd, keyToAdd+"");
        }
        actualBTree.print();
        // Assert
        for (long i = 0; i < maxValue; i++) {
            long key = i;
            String expected = expectedMap.get(key);
            String actual = actualBTree.search(key);
            if (!Objects.equals(expected, actual)) {
                actualBTree.print();
            }
            assertEquals(expected, actual, "Key " + key);
        }

    }
    @Test
    public void testMultipleDeletions() {
        BTree bTree = new BTree();
        TreeMap<Long, String> map = new TreeMap<>();
        // Вставка ключей
        for (long i = 0; i < 300; i++) {
            bTree.insert(i, i + "");
            map.put(i, i + "");
        }
        // Удаление ключей
        for (long i = 0; i < 300; i += 1) {
            bTree.delete(i);
            map.remove(i);
        }

        // Проверка
        for (long i = 0; i < 100; i++) {
            assertEquals(map.get(i), bTree.search(i), "Key " + i);
        }
    }
}
