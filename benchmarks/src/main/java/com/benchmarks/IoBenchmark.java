package com.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 2, warmups = 1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class IoBenchmark {

    @Param({"100", "1000"})
    private int lines;

    private Path tempFile;
    private String content;
    private InetSocketAddress serverAddress;
    private ExecutorService threadPool;

    @Setup
    public void setup() throws Exception {
        tempFile = Files.createTempFile("jmh-io-", ".txt");
        tempFile.toFile().deleteOnExit();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines; i++) {
            sb.append("line-").append(i).append(": ").append("data-").append(i % 10).append("\n");
        }
        content = sb.toString();
        Files.writeString(tempFile, content);

        threadPool = Executors.newCachedThreadPool();
    }

    @TearDown
    public void tearDown() throws Exception {
        Files.deleteIfExists(tempFile);
        threadPool.shutdown();
    }

    @Benchmark
    public void bufferedReaderRead(Blackhole bh) throws Exception {
        try (BufferedReader reader = Files.newBufferedReader(tempFile)) {
            String line;
            while ((line = reader.readLine()) != null) bh.consume(line);
        }
    }

    @Benchmark
    public void filesReadAllLines(Blackhole bh) throws Exception {
        List<String> allLines = Files.readAllLines(tempFile);
        for (String line : allLines) bh.consume(line);
    }

    @Benchmark
    public void filesReadString(Blackhole bh) throws Exception {
        bh.consume(Files.readString(tempFile));
    }

    @Benchmark
    public void fileInputStreamRead(Blackhole bh) throws Exception {
        try (FileInputStream fis = new FileInputStream(tempFile.toFile())) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = fis.read(buf)) != -1) bh.consume(n);
        }
    }

    @Benchmark
    public void bufferedOutputStreamWrite(Blackhole bh) throws Exception {
        Path out = Files.createTempFile("jmh-out-", ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(out)) {
            writer.write(content);
        }
        Files.deleteIfExists(out);
        bh.consume(out);
    }

    @Benchmark
    public void nioChannelRead(Blackhole bh) throws Exception {
        try (FileChannel channel = FileChannel.open(tempFile, StandardOpenOption.READ)) {
            ByteBuffer buf = ByteBuffer.allocate(4096);
            while (channel.read(buf) != -1) {
                buf.flip();
                bh.consume(buf.remaining());
                buf.clear();
            }
        }
    }

    @Benchmark
    public void nioChannelWrite(Blackhole bh) throws Exception {
        Path out = Files.createTempFile("jmh-nio-", ".txt");
        try (FileChannel channel = FileChannel.open(out, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            ByteBuffer buf = ByteBuffer.wrap(content.getBytes());
            while (buf.hasRemaining()) channel.write(buf);
        }
        Files.deleteIfExists(out);
        bh.consume(out);
    }

    @Benchmark
    public void nioMemoryMappedRead(Blackhole bh) throws Exception {
        try (FileChannel channel = FileChannel.open(tempFile, StandardOpenOption.READ)) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            byte[] buf = new byte[(int) channel.size()];
            mapped.get(buf);
            bh.consume(buf.length);
        }
    }

    @Benchmark
    public void bufferedWriterBufferedReader(Blackhole bh) throws Exception {
        Path out = Files.createTempFile("jmh-rw-", ".txt");
        Files.writeString(out, content);
        String read = Files.readString(out);
        Files.deleteIfExists(out);
        bh.consume(read.length());
    }

    @Benchmark
    public void nioFileCopy(Blackhole bh) throws Exception {
        Path out = Files.createTempFile("jmh-copy-", ".txt");
        Files.copy(tempFile, out, StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(out);
        bh.consume(out);
    }
}
