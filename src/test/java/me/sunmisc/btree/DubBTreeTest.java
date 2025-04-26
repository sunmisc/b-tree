package me.sunmisc.btree;

import me.sunmisc.btree.imm.BTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DubBTreeTest {
    @Test
    public void testAddAndPoll() {
        List<Long> map = new ArrayList<>();
        BTree bTree = new BTree();
        for (long i = 0; i < 10000; ++i) {
            map.add(i);
            bTree.insert(String.valueOf(i), i+"");
        }
        for (long i = 0; i < 10000; ++i) {
            long r = i + 10000;
            bTree.insert(String.valueOf(r), r+"");
            bTree.delete(bTree.getRoot().smallestKey());

            map.add(r);
            map.removeFirst();
        }
        for (long i = 0; i < 10000; ++i) {
            long key = i;
            int index = map.indexOf(key);
            String expected = index < 0 ? null : map.get(index)+"";
            String actual = bTree.search(String.valueOf(key));
            assertEquals(expected, actual, "Key " + key);
        }
    }

    @Test
    public void testAdd() {
        Map<Long, String> expectedMap = new HashMap<>();
        BTree bTree = new BTree();
        for (long i = 0; i < 10000; ++i) {
            expectedMap.put(i, i+"");
            bTree.insert(String.valueOf(i), i+"");
        }
        for (long i = 0; i < 10000; ++i) {
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
        int maxValue = 1000;

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
        int maxValue = 1000;

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
        for (long i = 0; i < 300; i++) {
            bTree.insert(String.valueOf(i), i + "");
            expectedMap.put(i, i + "");
        }
        for (long i = 0; i < 300; i += 1) {
            bTree.delete(String.valueOf(i));
            expectedMap.remove(i);
        }
        for (long i = 0; i < 100; i++) {
            assertEquals(expectedMap.get(i), bTree.search(String.valueOf(i)), "Key " + i);
        }
    }
}
