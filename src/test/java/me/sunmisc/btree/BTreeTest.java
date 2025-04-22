package me.sunmisc.btree;

import me.sunmisc.btree.imm.BTree;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Objects;
import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BTreeTest {

    @Test
    public void testAdd() {
        TreeMap<String, String> map = new TreeMap<>();
        BTree bTree = new BTree();
        for (int i = 0; i < 10000; ++i) {
            map.put(i+"", i+"");
            bTree.insert(i+"", i+"");
        }
        for (int i = 0; i < 10000; ++i) {
            if (!Objects.equals(map.get(i + ""), bTree.search(i + ""))) {
                throw new IllegalStateException();
            }
        }
    }
    @ParameterizedTest
    @ValueSource(ints = {1, 3, 4, 32})
    public void testRemoveAndAdd(int step) {
        TreeMap<String, String> expectedMap = new TreeMap<>();
        BTree actualBTree = new BTree();
        int maxValue = 1000;

        // Act - Initial insertion
        for (int i = 0; i < maxValue; i++) {
            String key = String.valueOf(i);
            expectedMap.put(key, key);
            actualBTree.insert(key, key);
        }

        // Act - Remove and add operations
        for (int i = 0; i < maxValue; i += step) {
            String keyToRemove = String.valueOf(i);
            String keyToAdd = String.valueOf(i + step);

            // Remove operation
            expectedMap.remove(keyToRemove);
            actualBTree.delete(keyToRemove);

            // Add operation
            expectedMap.put(keyToAdd, keyToAdd);
            actualBTree.insert(keyToAdd, keyToAdd);
        }
        // Assert
        for (int i = 0; i < maxValue; i++) {
            String key = String.valueOf(i);
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
        TreeMap<String, String> map = new TreeMap<>();
        // Вставка ключей
        for (int i = 0; i < 300; i++) {
            bTree.insert(i + "", i + "");
            map.put(i + "", i + "");
        }
        // Удаление ключей
        for (int i = 0; i < 300; i += 1) {
            bTree.delete(i + "");
            map.remove(i + "");
        }

        // Проверка
        for (int i = 0; i < 100; i++) {
            assertEquals(map.get(i + ""), bTree.search(i + ""), "Key " + i);
        }
    }
    public static void main(String[] args) {
        TreeMap<String, String> map = new TreeMap<>();
        BTree bTree = new BTree();
        for (int i = 0; i < 10000; ++i) {
            map.put(i+"", i+"");
            bTree.insert(i+"", i+"");
        }
        bTree.print();
        for (int i = 0; i < 64; i++) {
            System.out.println(Objects.equals(map.get(i + ""), bTree.search(i + "")));
        }
        for (int i = 0; i < 10000; i += 1) {
            map.remove(i + "");
            bTree.delete(i + "");

            map.put(i + 1+ "", i + 1 + "");
            bTree.insert(i + 1+ "", i + 1 + "");
        }
        System.out.println("REMOVED----------------------------");
        for (int i = 0; i < 10000; i++) {
            System.out.println(Objects.equals(map.get(i + ""), bTree.search(i + "")));
        }
        for (int i = 0; i < 64; i += 2) {
            map.put(i+"", i+"");
            bTree.insert(i+"", i+"");
        }
        for (int i = 0; i < 64; i++) {
            System.out.println(Objects.equals(map.get(i + ""), bTree.search(i + "")));
        }
    }
}
