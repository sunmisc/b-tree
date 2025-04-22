package me.sunmisc.btree;


import me.sunmisc.btree.imm.BTree;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
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
    @Param({"10", "10000", "1000000"})
    private int size;
    private BTree bTree;
    private Map<String, String> set;

    @Setup
    public void prepare() {
        bTree = new BTree();
        set = new TreeMap<>();
        for (int i = 0; i < size; ++i) {
            bTree.insert(i + "", i + "");
            set.put(i+"", i+"");
        }
        for (int i = 0; i < size; i += 3) {
            set.remove(i + "");
            bTree.delete(i + "");

            set.put(i + 1+ "", i + 1 + "");
            bTree.insert(i + 1+ "", i + 1 + "");
        }

    }

    @Benchmark
    public String readBtree() {
        int r = ThreadLocalRandom.current().nextInt(size);
        return bTree.search(r+"");
    }
    @Benchmark
    public String readRedBlack() {
        int r = ThreadLocalRandom.current().nextInt(size);
        return set.get(r+"");
    }
}
