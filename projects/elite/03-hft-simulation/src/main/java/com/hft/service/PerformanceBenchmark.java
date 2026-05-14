package com.hft.service;

import com.hft.model.OrderBook;
import com.hft.model.Side;
import com.hft.model.OrderType;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(1)
public class PerformanceBenchmark {

    private OrderBook orderBook;

    @Setup
    public void setup() {
        orderBook = new OrderBook(1);
        for (int i = 0; i < 1000; i++) {
            long price = ThreadLocalRandom.current().nextLong(90, 110);
            long qty = ThreadLocalRandom.current().nextLong(100, 1000);
            orderBook.addOrder(Side.BUY, price, qty, OrderType.LIMIT);
        }
        for (int i = 0; i < 1000; i++) {
            long price = ThreadLocalRandom.current().nextLong(90, 110);
            long qty = ThreadLocalRandom.current().nextLong(100, 1000);
            orderBook.addOrder(Side.SELL, price, qty, OrderType.LIMIT);
        }
    }

    @Benchmark
    public void benchmarkMatch() {
        orderBook.match();
    }

    @Benchmark
    public void benchmarkAddOrder() {
        long price = ThreadLocalRandom.current().nextLong(90, 110);
        long qty = ThreadLocalRandom.current().nextLong(100, 1000);
        Side side = ThreadLocalRandom.current().nextBoolean() ? Side.BUY : Side.SELL;
        orderBook.addOrder(side, price, qty, OrderType.LIMIT);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(PerformanceBenchmark.class.getSimpleName())
            .build();
        new Runner(opt).run();
    }
}
