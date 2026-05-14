package com.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, warmups = 1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class ConcurrencyBenchmark {

    @Param({"4", "8", "16"})
    private int threads;

    private ExecutorService threadPool;
    private ExecutorService virtualThreadPool;
    private Counter synchronizedCounter;
    private Counter reentrantCounter;
    private Counter stampedCounter;
    private BlockingQueue<Integer> arrayBlockingQueue;
    private BlockingQueue<Integer> linkedBlockingQueue;
    private BlockingQueue<Integer> priorityBlockingQueue;

    @Setup
    public void setup() {
        threadPool = Executors.newFixedThreadPool(threads);
        virtualThreadPool = Executors.newVirtualThreadPerTaskExecutor();
        synchronizedCounter = new SynchronizedCounter();
        reentrantCounter = new ReentrantLockCounter();
        stampedCounter = new StampedLockCounter();
        arrayBlockingQueue = new ArrayBlockingQueue<>(10000);
        linkedBlockingQueue = new LinkedBlockingQueue<>();
        priorityBlockingQueue = new PriorityBlockingQueue<>();
    }

    @TearDown
    public void tearDown() {
        threadPool.shutdown();
        virtualThreadPool.shutdown();
    }

    @Benchmark
    public long synchronizedIncrement() {
        return synchronizedCounter.incrementAndGet();
    }

    @Benchmark
    public long reentrantLockIncrement() {
        return reentrantCounter.incrementAndGet();
    }

    @Benchmark
    public long stampedLockIncrement() {
        return stampedCounter.incrementAndGet();
    }

    @Benchmark
    public void threadPoolSubmit(Blackhole bh) throws Exception {
        Future<Integer> future = threadPool.submit(() -> {
            int sum = 0;
            for (int i = 0; i < 100; i++) sum += i;
            return sum;
        });
        bh.consume(future.get());
    }

    @Benchmark
    public void virtualThreadSubmit(Blackhole bh) throws Exception {
        Future<Integer> future = virtualThreadPool.submit(() -> {
            int sum = 0;
            for (int i = 0; i < 100; i++) sum += i;
            return sum;
        });
        bh.consume(future.get());
    }

    @Benchmark
    public void threadStart(Blackhole bh) throws Exception {
        Thread t = new Thread(() -> bh.consume(Thread.currentThread().getName()));
        t.start();
        t.join();
    }

    @Benchmark
    public void virtualThreadStart(Blackhole bh) throws Exception {
        Thread t = Thread.ofVirtual().start(() -> bh.consume(Thread.currentThread().getName()));
        t.join();
    }

    @Benchmark
    public void arrayBlockingQueueOfferPoll() {
        arrayBlockingQueue.offer(1);
        arrayBlockingQueue.poll();
    }

    @Benchmark
    public void linkedBlockingQueueOfferPoll() {
        linkedBlockingQueue.offer(1);
        linkedBlockingQueue.poll();
    }

    @Benchmark
    public void priorityBlockingQueueOfferPoll() {
        priorityBlockingQueue.offer(1);
        priorityBlockingQueue.poll();
    }

    @Benchmark
    public void completableFutureSupply(Blackhole bh) throws Exception {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 42, threadPool);
        bh.consume(future.get());
    }

    @Benchmark
    public void completableFutureChain(Blackhole bh) throws Exception {
        CompletableFuture<Integer> future = CompletableFuture
                .supplyAsync(() -> 10, threadPool)
                .thenApplyAsync(v -> v * 2, threadPool)
                .thenApplyAsync(v -> v + 1, threadPool);
        bh.consume(future.get());
    }

    @Benchmark
    public void countDownLatch(Blackhole bh) throws Exception {
        int n = threads;
        CountDownLatch latch = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            threadPool.submit(() -> {
                latch.countDown();
                return null;
            });
        }
        latch.await();
        bh.consume(n);
    }

    interface Counter {
        long incrementAndGet();
    }

    static class SynchronizedCounter implements Counter {
        private long value;

        @Override
        public synchronized long incrementAndGet() {
            return ++value;
        }
    }

    static class ReentrantLockCounter implements Counter {
        private final Lock lock = new ReentrantLock();
        private long value;

        @Override
        public long incrementAndGet() {
            lock.lock();
            try {
                return ++value;
            } finally {
                lock.unlock();
            }
        }
    }

    static class StampedLockCounter implements Counter {
        private final StampedLock lock = new StampedLock();
        private long value;

        @Override
        public long incrementAndGet() {
            long stamp = lock.writeLock();
            try {
                return ++value;
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }
}
