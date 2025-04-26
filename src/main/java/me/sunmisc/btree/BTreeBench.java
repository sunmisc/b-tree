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
    private static final int SIZE = 1_000_000;

    @State(Scope.Thread)
    public static class RedBlackTreeState {
        TreeMap<String, String> map;

        @Setup
        public void prepare() {
            map = new TreeMap<>();
            for (int i = 1; i < SIZE; ++i) {
                long r = ThreadLocalRandom.current().nextInt(i);
                map.put(r+"", r+"");
            }
        }
    }

    @State(Scope.Thread)
    public static class BTreeState {
        BTree bTree;


        @Setup
        public void prepare() {
            bTree = new BTree();
            for (int i = 1; i < SIZE; ++i) {
                long r = ThreadLocalRandom.current().nextInt( i);
                bTree.insert(r+"", r+"");
            }
        }
    }


    @Benchmark
    public String readB(BTreeState state) {
        long r = ThreadLocalRandom.current().nextInt(SIZE);
        return state.bTree.search(String.valueOf(r));
    }

    @Benchmark
    public String putAndDeleteB(BTreeState state) {
        long r = ThreadLocalRandom.current().nextInt(SIZE);
        state.bTree.insert(r+"", r+"");
        for (int i = 0; i < 1; ++i) {
            state.bTree.delete(state.bTree.firstKey());
        }
        return r+"";
    }
    @Benchmark
    public String readRB(RedBlackTreeState state) {
        long r = ThreadLocalRandom.current().nextInt(SIZE);
        return state.map.get(String.valueOf(r));
    }

    @Benchmark
    public String putAndDeleteRB(RedBlackTreeState state) {
        long r = ThreadLocalRandom.current().nextInt(SIZE);
        state.map.put(r+"", r+"");
        for (int i = 0; i < 1; ++i) {
            state.map.pollFirstEntry();
        }
        return r+"";
    }
}
