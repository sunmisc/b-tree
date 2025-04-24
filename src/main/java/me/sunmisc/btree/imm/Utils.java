package me.sunmisc.btree.imm;

import java.util.*;

public final class Utils {

    public static <T> List<T> append(int idx, T value, List<T> list) {
        List<T> result = new ArrayList<>(list);
        result.add(idx, value);
        return result;
    }

    public static <T> List<T> set(int idx, T value, List<T> list) {
        List<T> result = new ArrayList<>(list);
        result.set(idx, value);
        return result;
    }

    public static <T> List<T> withoutIdx(int idx, List<T> list) {
        List<T> result = new ArrayList<>(list);
        if (result.isEmpty()) {
            return result;
        }
        result.remove(idx);
        return result;
    }

    public static <T> List<List<T>> splitAt(int idx, List<T> list) {
        List<T> left = new ArrayList<>(list.subList(0, idx));
        List<T> right = new ArrayList<>(list.subList(idx, list.size()));
        return List.of(left, right);
    }

    public static <T> List<T> copy(List<T> list) {
        return new ArrayList<>(list);
    }

    public static <T> List<T> tail(List<T> list) {
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(list.subList(1, list.size()));
    }

    public static <T> List<T> unshift(T value, List<T> list) {
        List<T> result = new ArrayList<>();
        result.add(value);
        result.addAll(list);
        return result;
    }

    public static void setRef(boolean[] ref) {
        ref[0] = true;
    }

    public static boolean isSet(boolean[] ref) {
        return ref[0];
    }
}
