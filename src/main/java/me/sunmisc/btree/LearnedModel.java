package me.sunmisc.btree;

import java.util.*;

public class LearnedModel {
    private final double slope;
    private final double intercept;
    private double meanAbsError = 1.0;

    public LearnedModel(double slope, double intercept) {
        this.slope = slope;
        this.intercept = intercept;
    }

    public int predict(int key) {
        return (int) Math.round(slope * key + intercept);
    }

    public static LearnedModel retrain(List<Integer> sortedKeys) {
        int n = sortedKeys.size();
        if (n == 0) return new LearnedModel(0, 0);

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
        if (Math.abs(denominator) < 1e-9) {
            return new LearnedModel(0, 0);
        }

        double slope = (n * sumXY - sumX * sumY) / denominator;
        double intercept = (sumY - slope * sumX) / n;

        LearnedModel model = new LearnedModel(slope, intercept);
        model.meanAbsError = model.computeMAE(sortedKeys);
        return model;
    }

    public double computeMAE(List<Integer> sortedKeys) {
        double error = 0;
        for (int i = 0; i < sortedKeys.size(); i++) {
            int predicted = predict(sortedKeys.get(i));
            error += Math.abs(predicted - i);
        }
        return error / sortedKeys.size();
    }

    public boolean needsRetrain(List<Integer> keys, double threshold) {
        return computeMAE(keys) > threshold;
    }

    public int search(List<Integer> sortedKeys, int key) {
        if (sortedKeys.isEmpty()) return -1;

        int guess = predict(key);
        int window = Math.max(2, (int) Math.ceil(meanAbsError * 2));

        int low = Math.max(0, guess - window);
        int high = Math.min(sortedKeys.size() - 1, guess + window);

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = sortedKeys.get(mid);
            if (midVal < key) {
                low = mid + 1;
            } else if (midVal > key) {
                high = mid - 1;
            } else {
                return mid; // найден
            }
        }

        // Если не нашли, то...
        // fallback на стандартную binarySearch, чтобы совпадал поведение
        return Collections.binarySearch(sortedKeys, key);
    }


}