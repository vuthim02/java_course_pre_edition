package com.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 2, warmups = 1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class CollectionBenchmark {

    @Param({"100", "1000", "10000"})
    private int size;

    private List<Integer> arrayList;
    private List<Integer> linkedList;
    private Map<Integer, String> hashMap;
    private Map<Integer, String> treeMap;
    private Map<Integer, String> concurrentHashMap;

    @Setup
    public void setup() {
        arrayList = new ArrayList<>();
        linkedList = new LinkedList<>();
        hashMap = new HashMap<>();
        treeMap = new TreeMap<>();
        concurrentHashMap = new ConcurrentHashMap<>();

        for (int i = 0; i < size; i++) {
            arrayList.add(i);
            linkedList.add(i);
            hashMap.put(i, "value-" + i);
            treeMap.put(i, "value-" + i);
            concurrentHashMap.put(i, "value-" + i);
        }
    }

    @Benchmark
    public void arrayListAdd(Blackhole bh) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) list.add(i);
        bh.consume(list);
    }

    @Benchmark
    public void linkedListAdd(Blackhole bh) {
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < 1000; i++) list.add(i);
        bh.consume(list);
    }

    @Benchmark
    public void arrayListGet(Blackhole bh) {
        for (int i = 0; i < size; i++) bh.consume(arrayList.get(i));
    }

    @Benchmark
    public void linkedListGet(Blackhole bh) {
        for (int i = 0; i < size; i++) bh.consume(linkedList.get(i));
    }

    @Benchmark
    public void arrayListRemove(Blackhole bh) {
        List<Integer> copy = new ArrayList<>(arrayList);
        while (!copy.isEmpty()) copy.remove(0);
        bh.consume(copy);
    }

    @Benchmark
    public void linkedListRemove(Blackhole bh) {
        List<Integer> copy = new LinkedList<>(linkedList);
        while (!copy.isEmpty()) copy.remove(0);
        bh.consume(copy);
    }

    @Benchmark
    public void hashMapPut(Blackhole bh) {
        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < 1000; i++) map.put(i, "v");
        bh.consume(map);
    }

    @Benchmark
    public void treeMapPut(Blackhole bh) {
        Map<Integer, String> map = new TreeMap<>();
        for (int i = 0; i < 1000; i++) map.put(i, "v");
        bh.consume(map);
    }

    @Benchmark
    public void concurrentHashMapPut(Blackhole bh) {
        Map<Integer, String> map = new ConcurrentHashMap<>();
        for (int i = 0; i < 1000; i++) map.put(i, "v");
        bh.consume(map);
    }

    @Benchmark
    public void hashMapGet(Blackhole bh) {
        for (int i = 0; i < size; i++) bh.consume(hashMap.get(i));
    }

    @Benchmark
    public void treeMapGet(Blackhole bh) {
        for (int i = 0; i < size; i++) bh.consume(treeMap.get(i));
    }

    @Benchmark
    public void concurrentHashMapGet(Blackhole bh) {
        for (int i = 0; i < size; i++) bh.consume(concurrentHashMap.get(i));
    }

    @Benchmark
    public void streamForEach(Blackhole bh) {
        arrayList.forEach(bh::consume);
    }

    @Benchmark
    public void loopForEach(Blackhole bh) {
        for (int v : arrayList) bh.consume(v);
    }

    @Benchmark
    public void streamMapCollect(Blackhole bh) {
        List<String> result = arrayList.stream()
                .map(i -> "item-" + i)
                .collect(Collectors.toList());
        bh.consume(result);
    }

    @Benchmark
    public void parallelStreamMapCollect(Blackhole bh) {
        List<String> result = arrayList.parallelStream()
                .map(i -> "item-" + i)
                .collect(Collectors.toList());
        bh.consume(result);
    }

    @Benchmark
    public void streamFilterCount(Blackhole bh) {
        long count = arrayList.stream().filter(i -> i % 2 == 0).count();
        bh.consume(count);
    }

    @Benchmark
    public void parallelStreamFilterCount(Blackhole bh) {
        long count = arrayList.parallelStream().filter(i -> i % 2 == 0).count();
        bh.consume(count);
    }
}
