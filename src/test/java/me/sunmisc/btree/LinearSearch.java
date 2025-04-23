package me.sunmisc.btree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinearSearch {
    public static void main(String[] args) {
        List<Integer> keys = Arrays.asList(5, 12, 12232, 14234324, 1232344551, 1322232312);
        LearnedModel model = LearnedModel.retrain(keys);

        System.out.println("Model: " + model);
        System.out.println("MAE: " + model.computeMAE(keys));

        int predicted = model.search(keys, 3333);
        System.out.println(predicted + " " + Collections.binarySearch(keys, 3333));
        // Проверим предсказания
        for (int k : keys) {
            predicted = model.search(keys, k);
            System.out.printf("Key: %d → Predicted Index: %d%n", k, predicted);
        }

        // Добавим "аномальный" ключ и проверим, что нужно переобучить
        List<Integer> changedKeys = new ArrayList<>(keys);
        changedKeys.set(4, 999); // заменили 50 на 999

        boolean retrainNeeded = model.needsRetrain(changedKeys, 1.0);
        System.out.println("Needs retrain after mutation? " + retrainNeeded);
    }
    @ParameterizedTest
    @ValueSource(ints = {16, 64, 128})
    public void test(int size) {
        List<Integer> keys = ThreadLocalRandom.current()
                .ints(size, 0, 100_000)
                .distinct()
                .sorted()
                .boxed()
                .toList();
        LearnedModel model = LearnedModel.retrain(keys);
        for (int k : keys) {
            int expected = model.search(keys, k);
            int actual = Collections.binarySearch(keys, k);
            assertEquals(expected, actual, "Key " + k +
                    " expected " + expected + " actual " + actual);
        }
        List<Integer> test = ThreadLocalRandom.current()
                .ints(size, 0, 64)
                .distinct()
                .boxed()
                .sorted()
                .toList();
        for (int k : test) {
            int expected = model.search(test, k);
            int actual = Collections.binarySearch(test, k);
            assertEquals(expected, actual, "Key " + k +
                    " expected " + expected + " actual " + actual);
        }
    }
}
