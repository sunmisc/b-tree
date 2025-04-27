package me.sunmisc.btree;

import me.sunmisc.btree.imm.BTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BTreeTest {

    private static final int MAX_SIZE = 10_000;
    @Test
    public void testAddAndPoll() {
        TreeMap<String, String> map = new TreeMap<>();
        BTree bTree = new BTree();
        for (long i = 0; i < MAX_SIZE; ++i) {
            map.put(i+"", i+"");
            bTree.insert(String.valueOf(i), i+"");
        }
        for (long i = 0; i < MAX_SIZE; ++i) {
            long r = i + MAX_SIZE;
            bTree.insert(String.valueOf(r), r+"");
            bTree.delete(bTree.firstKey());

            map.put(r+"", r+"");
            map.pollFirstEntry();
        }
        for (long i = 0; i < MAX_SIZE; ++i) {
            long key = i;
            String expected = map.get(key+"");
            String actual = bTree.search(String.valueOf(key));
            if (!Objects.equals(expected, actual)) {
                bTree.print();
            }
            assertEquals(expected, actual, "Key " + key);
        }
    }

    @Test
    public void testAdd() {
        Map<Long, String> expectedMap = new HashMap<>();
        BTree bTree = new BTree();
        for (long i = 0; i < MAX_SIZE; ++i) {
            expectedMap.put(i, i+"");
            bTree.insert(String.valueOf(i), i+"");
        }
        for (long i = 0; i < MAX_SIZE; ++i) {
            long key = i;
            String expected = expectedMap.get(key);
            String actual = bTree.search(String.valueOf(key));
            assertEquals(expected, actual, "Key " + key);
        }
    }
    @ParameterizedTest
    @ValueSource(ints = {1, 3, 3, 5, 32, 63})
    public void testRemoveAndAddRand(int step) {
        Map<Long, String> expectedMap = new HashMap<>();
        BTree actualBTree = new BTree();
        int maxValue = MAX_SIZE;

        // Act - Initial insertion
        for (int i = 0; i < maxValue; i++) {
            long key = ThreadLocalRandom.current().nextInt(1_000_000);
            expectedMap.put(key, key+"");
            actualBTree.insert(String.valueOf(key), key+"");
        }

        actualBTree.print();
        // Act - Remove and add operations
        for (long i = 0; i < maxValue; i += step) {
            long keyToRemove = i;
            long keyToAdd = i + step;

            // Remove operation
            expectedMap.remove(keyToRemove);
            actualBTree.delete(String.valueOf(keyToRemove));

            // Add operation
            expectedMap.put(keyToAdd, keyToAdd+"");
            actualBTree.insert(String.valueOf(keyToAdd), keyToAdd+"");
        }
        // Assert
        for (long i = 0; i < maxValue; i++) {
            long key = i;
            String expected = expectedMap.get(key);
            String actual = actualBTree.search(String.valueOf(key));
            if (!Objects.equals(expected, actual)) {
                actualBTree.print();
            }
            assertEquals(expected, actual, "Key " + key);
        }

    }
    @ParameterizedTest
    @ValueSource(ints = {1, 3, 3, 5, 32, 63})
    public void testRemoveAndAdd(int step) {
        Map<Long, String> expectedMap = new HashMap<>();
        BTree actualBTree = new BTree();
        int maxValue = MAX_SIZE;

        // Act - Initial insertion
        for (long i = 0; i < maxValue; i++) {
            long key = i;
            expectedMap.put(key, key+"");
            actualBTree.insert(String.valueOf(key), key+"");
        }

        // Act - Remove and add operations
        for (long i = 0; i < maxValue; i += step) {
            long keyToRemove = i;
            long keyToAdd = i + step;

            // Remove operation
            expectedMap.remove(keyToRemove);
            actualBTree.delete(String.valueOf(keyToRemove));

            // Add operation
            expectedMap.put(keyToAdd, keyToAdd+"");
            actualBTree.insert(String.valueOf(keyToAdd), keyToAdd+"");
        }
        actualBTree.print();
        // Assert
        for (long i = 0; i < maxValue; i++) {
            long key = i;
            String expected = expectedMap.get(key);
            String actual = actualBTree.search(String.valueOf(key));
            if (!Objects.equals(expected, actual)) {
                actualBTree.print();
            }
            assertEquals(expected, actual, "Key " + key);
        }

    }
    @Test
    public void testMultipleDeletions() {
        BTree bTree = new BTree();
        Map<Long, String> expectedMap = new HashMap<>();
        for (long i = 0; i < MAX_SIZE; i++) {
            bTree.insert(String.valueOf(i), i + "");
            expectedMap.put(i, i + "");
        }
        for (long i = 0; i < MAX_SIZE; i += 1) {
            bTree.delete(String.valueOf(i));
            expectedMap.remove(i);
        }
        for (long i = 0; i < MAX_SIZE; i++) {
            assertEquals(expectedMap.get(i), bTree.search(String.valueOf(i)), "Key " + i);
        }
    }
}
