package me.sunmisc.btree;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 8, time = 1)
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

    private List<Integer> keys;
    private LearnedModel linearModel;

    @Setup
    public void prepare() {
 /*       keys = ThreadLocalRandom.current().ints(100_000,
                0, 100_000).boxed().toList();*/
        keys = IntStream.range(0, 100_000)
                .map(e -> e +
                        ThreadLocalRandom.current().nextInt(0, 120))
                .sorted()
                .distinct()
                .boxed().toList();
        linearModel = LearnedModel.retrain(keys);
    }

    @Benchmark
    public int linearSearch() {
        int r = ThreadLocalRandom.current().nextInt(keys.size());
        return linearModel.search(keys, r);
    }
    @Benchmark
    public int binSearch() {
        int r = ThreadLocalRandom.current().nextInt(keys.size());
        return Collections.binarySearch(keys, r);
    }
}
