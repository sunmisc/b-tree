package me.sunmisc.btree;

import java.util.*;

public final class LearnedModel {
    private final double slope;
    private final double intercept;
    private final double meanAbsError;

    private LearnedModel(double slope, double intercept, double meanAbsError) {
        this.slope = slope;
        this.intercept = intercept;
        this.meanAbsError = meanAbsError;
    }

    public int predict(long key, int upperBound) {
        double raw = slope * key + intercept;
        long result = Math.round(raw);
        if (result < 0) return 0;
        if (result >= upperBound) return upperBound - 1;
        return (int) result;
    }

    public static LearnedModel retrain(List<Long> sortedKeys) {
        int n = sortedKeys.size();
        if (n == 0) return new LearnedModel(0, 0, 1.0);

        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int i = 0; i < n; i++) {
            double x = sortedKeys.get(i);
            double y = i;
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double denominator = n * sumXX - sumX * sumX;
        double slope, intercept;

        if (Math.abs(denominator) < 1e-9) {
            // Все ключи одинаковые — предсказываем центр массива
            slope = 0;
            intercept = n / 2.0;
        } else {
            slope = (n * sumXY - sumX * sumY) / denominator;
            intercept = (sumY - slope * sumX) / n;
        }

        // Вычисление MAE при обучении
        double totalError = 0;
        for (int i = 0; i < n; i++) {
            double predicted = slope * sortedKeys.get(i) + intercept;
            totalError += Math.abs(predicted - i);
        }

        double meanAbsError = totalError / n;
        return new LearnedModel(slope, intercept, meanAbsError);
    }


    public int searchEq(List<Long> sortedKeys, long key) {
        if (learned) {
            if (sortedKeys.isEmpty()) return -1;

            int guess = predict(key, sortedKeys.size());

            int window = Math.max(2, (int) Math.ceil(meanAbsError * 2));
            int low = Math.max(0, guess - window);
            int high = Math.min(sortedKeys.size() - 1, guess + window);

            // Основной диапазон
            int index = binarySearch(sortedKeys, key, low, high);
            if (index >= 0) return index;

            // Левая часть (на случай недо-ошибки)
            if (low > 0) {
                index = binarySearch(sortedKeys, key, 0, low - 1);
                if (index >= 0) return index;
            }

            // Правая часть (на случай пере-ошибки)
            if (high < sortedKeys.size() - 1) {
                index = binarySearch(sortedKeys, key, high + 1, sortedKeys.size() - 1);
                if (index >= 0) return index;
            }

            return -1;
        } else {
            return binSearch(sortedKeys, key);
        }
    }

    // Вспомогательный метод бинарного поиска
    private static int binarySearch(List<Long> list, long key, int fromIndex, int toIndex) {
        int low = fromIndex;
        int high = toIndex;
        while (low <= high) {
            int mid = (low + high) >>> 1; // Безопасное вычисление середины
            long midVal = list.get(mid);
            if (midVal < key) {
                low = mid + 1;
            } else if (midVal > key) {
                high = mid - 1;
            } else {
                return mid; // Ключ найден
            }
        }
        return -(low + 1);
    }
    public static boolean learned = true;
    public int search(List<Long> sortedKeys, long key) {
        if (learned) {
            if (sortedKeys.isEmpty()) {
                return -1;
            }
            int guess = predict(key, sortedKeys.size());
            int window = Math.max(2, (int) Math.ceil(meanAbsError * 2));
            int low = Math.max(0, guess - window);
            int high = Math.min(guess + window, sortedKeys.size() - 1);

            while (low <= high) {
                int mid = (low + high) >>> 1;
                long midVal = sortedKeys.get(mid);
                if (midVal < key) {
                    low = mid + 1;
                } else if (midVal > key) {
                    high = mid - 1;
                } else {
                    return mid;
                }
            }
        }
        return binSearch(sortedKeys, key);
    }

    public static int binSearch(List<Long> list, long key) {
        return binarySearch(list, key, 0, list.size() - 1);
    }
}