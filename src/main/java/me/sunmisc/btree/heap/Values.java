package me.sunmisc.btree.heap;

import me.sunmisc.btree.index.Index;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public final class Values {
    private static final int BLOCK_SIZE = 1 << 12;
    private final AtomicLong additions = new AtomicLong();
    private final Queue<Long> removals = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        System.out.println(BLOCK_SIZE);
    }

    public Index alloc(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int needs = Math.floorDiv(bytes.length, BLOCK_SIZE);
        return null;
    }

    public String find(Index index) {
        return "";
    }

    public Optional<Index> tail() {
        return Optional.empty();
    }
}
