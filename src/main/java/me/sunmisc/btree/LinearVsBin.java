package me.sunmisc.btree;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 6, time = 1)
@Fork(1)
@Threads(1)
@BenchmarkMode({Mode.Throughput})
@State(Scope.Thread)
public class LinearVsBin {

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(LinearVsBin.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    private static final Random random = new Random(12822);
    @Param({"1", "2", "64", "1024", "4096"})
    private int spread;
    private List<Long> keys;
    private LearnedModel linearModel;

    @Setup
    public void prepare() {
        keys = LongStream.range(0, 256)
                .map(e -> e + random.nextInt(0, spread))
                .sorted()
                .distinct()
                .boxed()
                .toList();
        linearModel = LearnedModel.retrain(keys);
    }

    @Benchmark
    public int linearSearch() {
        int r = ThreadLocalRandom.current().nextInt(keys.size());
        return linearModel.searchEq(keys, r);
    }

    @Benchmark
    public int binSearch() {
        long r = ThreadLocalRandom.current().nextInt(keys.size());
        return LearnedModel.binSearch(keys, r);
    }
}
