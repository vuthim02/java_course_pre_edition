package com.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, warmups = 1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class HftBenchmark {

    @Param({"10000", "100000"})
    private int orders;

    private LockFreeOrderBook lockFreeBook;
    private LockedOrderBook lockedBook;
    private List<Order> buyOrders;
    private List<Order> sellOrders;
    private MarketDataFeed feed;

    @Setup
    public void setup() {
        lockFreeBook = new LockFreeOrderBook();
        lockedBook = new LockedOrderBook();
        buyOrders = new ArrayList<>();
        sellOrders = new ArrayList<>();
        feed = new MarketDataFeed();

        Random rnd = new Random(42);
        for (int i = 0; i < orders; i++) {
            double price = 100.0 + rnd.nextDouble() * 10.0;
            int quantity = rnd.nextInt(100) + 1;
            if (rnd.nextBoolean()) {
                buyOrders.add(new Order(i, true, price, quantity, System.nanoTime()));
            } else {
                sellOrders.add(new Order(i, false, price, quantity, System.nanoTime()));
            }
        }

        for (Order o : buyOrders) lockFreeBook.add(o);
        for (Order o : sellOrders) lockFreeBook.add(o);
        for (Order o : buyOrders) lockedBook.add(o);
        for (Order o : sellOrders) lockedBook.add(o);
    }

    @Benchmark
    public int lockFreeMatch() {
        return lockFreeBook.match();
    }

    @Benchmark
    public int lockedMatch() {
        return lockedBook.match();
    }

    @Benchmark
    public void lockFreeAdd(Blackhole bh) {
        Order o = new Order(-1, true, 105.0, 10, System.nanoTime());
        lockFreeBook.add(o);
        bh.consume(o);
    }

    @Benchmark
    public void lockedAdd(Blackhole bh) {
        Order o = new Order(-1, true, 105.0, 10, System.nanoTime());
        lockedBook.add(o);
        bh.consume(o);
    }

    @Benchmark
    public void lockFreeCancel(Blackhole bh) {
        Order o = buyOrders.get(0);
        lockFreeBook.cancel(o.id);
        bh.consume(o.id);
    }

    @Benchmark
    public void lockedCancel(Blackhole bh) {
        Order o = buyOrders.get(0);
        lockedBook.cancel(o.id);
        bh.consume(o.id);
    }

    @Benchmark
    public double lockFreeSpread() {
        return lockFreeBook.spread();
    }

    @Benchmark
    public double lockedSpread() {
        return lockedBook.spread();
    }

    @Benchmark
    public void marketDataProcess(Blackhole bh) {
        feed.process("AAPL", 150.25, 100, System.nanoTime());
        feed.process("GOOGL", 2750.50, 50, System.nanoTime());
        feed.process("MSFT", 350.75, 200, System.nanoTime());
        bh.consume(feed.getLastPrice("AAPL"));
    }

    @Benchmark
    public void atomicLongIncrement(Blackhole bh) {
        AtomicLong counter = new AtomicLong();
        for (int i = 0; i < 1000; i++) bh.consume(counter.incrementAndGet());
    }

    @Benchmark
    public void longAdderIncrement(Blackhole bh) {
        LongAdder adder = new LongAdder();
        for (int i = 0; i < 1000; i++) {
            adder.increment();
            bh.consume(adder.sum());
        }
    }

    @Benchmark
    public void copyOnWriteIteration(Blackhole bh) {
        List<Integer> list = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 100; i++) list.add(i);
        for (int v : list) bh.consume(v);
    }

    static class Order {
        final long id;
        final boolean isBuy;
        final double price;
        final int quantity;
        final long timestamp;

        Order(long id, boolean isBuy, double price, int quantity, long timestamp) {
            this.id = id;
            this.isBuy = isBuy;
            this.price = price;
            this.quantity = quantity;
            this.timestamp = timestamp;
        }
    }

    static class LockFreeOrderBook {
        private final ConcurrentNavigableMap<Double, Queue<Order>> bids = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        private final ConcurrentNavigableMap<Double, Queue<Order>> asks = new ConcurrentSkipListMap<>();
        private final AtomicLong orderIdGen = new AtomicLong();
        private final ConcurrentHashMap<Long, Order> ordersById = new ConcurrentHashMap<>();

        void add(Order o) {
            Order order = new Order(orderIdGen.incrementAndGet(), o.isBuy, o.price, o.quantity, o.timestamp);
            ordersById.put(order.id, order);
            Map<Double, Queue<Order>> book = order.isBuy ? bids : asks;
            book.computeIfAbsent(order.price, k -> new ConcurrentLinkedQueue<>()).add(order);
        }

        int match() {
            int matched = 0;
            while (!bids.isEmpty() && !asks.isEmpty()) {
                Map.Entry<Double, Queue<Order>> bestBid = bids.firstEntry();
                Map.Entry<Double, Queue<Order>> bestAsk = asks.firstEntry();
                if (bestBid == null || bestAsk == null) break;
                if (bestBid.getKey() < bestAsk.getKey()) break;
                Order bid = bestBid.getValue().poll();
                Order ask = bestAsk.getValue().poll();
                if (bid == null || ask == null) break;
                if (bestBid.getValue().isEmpty()) bids.remove(bestBid.getKey());
                if (bestAsk.getValue().isEmpty()) asks.remove(bestAsk.getKey());
                matched++;
            }
            return matched;
        }

        void cancel(long orderId) {
            Order o = ordersById.remove(orderId);
            if (o != null) {
                Map<Double, Queue<Order>> book = o.isBuy ? bids : asks;
                Queue<Order> queue = book.get(o.price);
                if (queue != null) queue.remove(o);
            }
        }

        double spread() {
            if (bids.isEmpty() || asks.isEmpty()) return Double.NaN;
            return asks.firstKey() - bids.firstKey();
        }
    }

    static class LockedOrderBook {
        private final TreeMap<Double, Queue<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
        private final TreeMap<Double, Queue<Order>> asks = new TreeMap<>();
        private final Lock lock = new ReentrantLock();
        private final StampedOrderBook stamped = new StampedOrderBook();

        void add(Order o) {
            lock.lock();
            try {
                Map<Double, Queue<Order>> book = o.isBuy ? bids : asks;
                book.computeIfAbsent(o.price, k -> new LinkedList<>()).add(o);
            } finally {
                lock.unlock();
            }
        }

        int match() {
            lock.lock();
            try {
                int matched = 0;
                while (!bids.isEmpty() && !asks.isEmpty()) {
                    Map.Entry<Double, Queue<Order>> bestBid = bids.firstEntry();
                    Map.Entry<Double, Queue<Order>> bestAsk = asks.firstEntry();
                    if (bestBid.getKey() < bestAsk.getKey()) break;
                    Order bid = bestBid.getValue().poll();
                    Order ask = bestAsk.getValue().poll();
                    if (bid == null || ask == null) break;
                    if (bestBid.getValue().isEmpty()) bids.remove(bestBid.getKey());
                    if (bestAsk.getValue().isEmpty()) asks.remove(bestAsk.getKey());
                    matched++;
                }
                return matched;
            } finally {
                lock.unlock();
            }
        }

        void cancel(long orderId) {
            lock.lock();
            try {
                for (Map<Double, Queue<Order>> book : Arrays.asList(bids, asks)) {
                    for (Map.Entry<Double, Queue<Order>> e : book.entrySet()) {
                        Iterator<Order> it = e.getValue().iterator();
                        while (it.hasNext()) {
                            if (it.next().id == orderId) { it.remove(); return; }
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        double spread() {
            lock.lock();
            try {
                if (bids.isEmpty() || asks.isEmpty()) return Double.NaN;
                return asks.firstKey() - bids.firstKey();
            } finally {
                lock.unlock();
            }
        }

        static class StampedOrderBook {
            private final StampedLock stampedLock = new StampedLock();
            private final TreeMap<Double, Queue<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
            private final TreeMap<Double, Queue<Order>> asks = new TreeMap<>();

            void add(Order o) {
                long stamp = stampedLock.writeLock();
                try {
                    Map<Double, Queue<Order>> book = o.isBuy ? bids : asks;
                    book.computeIfAbsent(o.price, k -> new LinkedList<>()).add(o);
                } finally {
                    stampedLock.unlockWrite(stamp);
                }
            }
        }
    }

    static class MarketDataFeed {
        private final ConcurrentHashMap<String, MarketData> latest = new ConcurrentHashMap<>();

        void process(String symbol, double price, int volume, long timestamp) {
            latest.put(symbol, new MarketData(symbol, price, volume, timestamp));
        }

        double getLastPrice(String symbol) {
            MarketData md = latest.get(symbol);
            return md != null ? md.price : Double.NaN;
        }

        record MarketData(String symbol, double price, int volume, long timestamp) {}
    }
}
