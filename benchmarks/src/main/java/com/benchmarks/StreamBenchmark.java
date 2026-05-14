package com.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 2, warmups = 1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class StreamBenchmark {

    @Param({"1000", "100000", "1000000"})
    private int size;

    private List<Integer> data;

    @Setup
    public void setup() {
        data = IntStream.range(0, size)
                .map(i -> (int) (Math.random() * size))
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Benchmark
    public void sequentialSum(Blackhole bh) {
        bh.consume(data.stream().mapToInt(Integer::intValue).sum());
    }

    @Benchmark
    public void parallelSum(Blackhole bh) {
        bh.consume(data.parallelStream().mapToInt(Integer::intValue).sum());
    }

    @Benchmark
    public void sequentialFilterMapReduce(Blackhole bh) {
        int result = data.stream()
                .filter(i -> i % 2 == 0)
                .map(i -> i * 2)
                .reduce(0, Integer::sum);
        bh.consume(result);
    }

    @Benchmark
    public void parallelFilterMapReduce(Blackhole bh) {
        int result = data.parallelStream()
                .filter(i -> i % 2 == 0)
                .map(i -> i * 2)
                .reduce(0, Integer::sum);
        bh.consume(result);
    }

    @Benchmark
    public void sequentialDistinct(Blackhole bh) {
        bh.consume(data.stream().distinct().count());
    }

    @Benchmark
    public void parallelDistinct(Blackhole bh) {
        bh.consume(data.parallelStream().distinct().count());
    }

    @Benchmark
    public void sequentialSorted(Blackhole bh) {
        List<Integer> sorted = data.stream().sorted().collect(Collectors.toList());
        bh.consume(sorted);
    }

    @Benchmark
    public void parallelSorted(Blackhole bh) {
        List<Integer> sorted = data.parallelStream().sorted().collect(Collectors.toList());
        bh.consume(sorted);
    }

    @Benchmark
    public void sequentialGroupBy(Blackhole bh) {
        Map<Integer, List<Integer>> grouped = data.stream()
                .collect(Collectors.groupingBy(i -> i % 10));
        bh.consume(grouped.size());
    }

    @Benchmark
    public void parallelGroupBy(Blackhole bh) {
        Map<Integer, List<Integer>> grouped = data.parallelStream()
                .collect(Collectors.groupingBy(i -> i % 10));
        bh.consume(grouped.size());
    }

    @Benchmark
    public void sequentialFlatMap(Blackhole bh) {
        long count = data.stream()
                .flatMap(i -> Arrays.stream(new String[]{"a-" + i, "b-" + i}))
                .count();
        bh.consume(count);
    }

    @Benchmark
    public void parallelFlatMap(Blackhole bh) {
        long count = data.parallelStream()
                .flatMap(i -> Arrays.stream(new String[]{"a-" + i, "b-" + i}))
                .count();
        bh.consume(count);
    }

    @Benchmark
    public void sequentialFindFirst(Blackhole bh) {
        bh.consume(data.stream().filter(i -> i > size / 2).findFirst());
    }

    @Benchmark
    public void sequentialLimitSkip(Blackhole bh) {
        List<Integer> result = data.stream()
                .skip(10)
                .limit(100)
                .collect(Collectors.toList());
        bh.consume(result);
    }

    @Benchmark
    public void sequentialMapToObj(Blackhole bh) {
        List<String> result = data.stream()
                .map(i -> "number-" + i)
                .collect(Collectors.toList());
        bh.consume(result);
    }

    @Benchmark
    public void parallelMapToObj(Blackhole bh) {
        List<String> result = data.parallelStream()
                .map(i -> "number-" + i)
                .collect(Collectors.toList());
        bh.consume(result);
    }

    @Benchmark
    public void sequentialAllMatch(Blackhole bh) {
        bh.consume(data.stream().allMatch(i -> i >= 0));
    }

    @Benchmark
    public void sequentialForEach(Blackhole bh) {
        data.stream().forEach(bh::consume);
    }

    @Benchmark
    public void loopForIndex(Blackhole bh) {
        for (int i = 0; i < data.size(); i++) bh.consume(data.get(i));
    }

    @Benchmark
    public void loopForEachEnhanced(Blackhole bh) {
        for (int v : data) bh.consume(v);
    }
}
