package me.sunmisc.btree;

import me.sunmisc.btree.imm.BTree;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 8, time = 1)
@Fork(1)
@Threads(1)
@BenchmarkMode({Mode.Throughput})
public class BTreeBench {
    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(BTreeBench.class.getSimpleName())
                // .syncIterations(false)
                // .addProfiler(GCProfiler.class)
                .build();
        new Runner(opt).run();
    }
    private static final int SIZE = 100_000;

    @State(Scope.Thread)
    public static class RedBlackTreeState {
        TreeMap<String, String> map;

        @Setup
        public void prepare() {
            map = new TreeMap<>();
            for (int i = 0; i < SIZE; ++i) {
                map.put(i+"", i+"");
            }
        }
    }

    @State(Scope.Thread)
    public static class BTreeState {
        BTree bTree;

        @Setup
        public void prepare() {
            bTree = new BTree();
            for (long i = 0; i < SIZE; ++i) {
                bTree.insert(i, i+"");
            }
        }
    }


    @Benchmark
    public String readB(BTreeState state) {
        long r = ThreadLocalRandom.current().nextInt(SIZE);
        return state.bTree.search(r);
    }
    @Benchmark
    public String readRb(RedBlackTreeState state) {
        long r = ThreadLocalRandom.current().nextInt(SIZE);
        return state.map.get(r+"");
    }

    @Benchmark
    public String putAndDeleteRb(RedBlackTreeState state) {
        long r = ThreadLocalRandom.current().nextInt(SIZE);
        state.map.put(r+"", r+"");
        state.map.pollFirstEntry();
        return r+"";
    }

    @Benchmark
    public String putAndDeleteB(BTreeState state) {
        long r = ThreadLocalRandom.current().nextInt(SIZE);
        state.bTree.insert(r, r+"");
        state.bTree.delete(state.bTree.getRoot().smallestKey());
        return r+"";
    }
}
