package com.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 2, warmups = 1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class StringBenchmark {

    @Param({"100", "1000"})
    private int count;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private String testString;
    private String searchString;
    private String[] words;

    @Setup
    public void setup() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append("word").append(i).append(" ");
        testString = sb.toString();
        searchString = "word" + (count / 2);
        words = new String[count];
        for (int i = 0; i < count; i++) words[i] = "value-" + i;
    }

    @Benchmark
    public void stringConcat(Blackhole bh) {
        String s = "";
        for (int i = 0; i < 100; i++) s = s + words[i];
        bh.consume(s);
    }

    @Benchmark
    public void stringBuilder(Blackhole bh) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) sb.append(words[i]);
        bh.consume(sb.toString());
    }

    @Benchmark
    public void stringBuffer(Blackhole bh) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 100; i++) sb.append(words[i]);
        bh.consume(sb.toString());
    }

    @Benchmark
    public void stringContains(Blackhole bh) {
        bh.consume(testString.contains(searchString));
    }

    @Benchmark
    public void stringIndexOf(Blackhole bh) {
        bh.consume(testString.indexOf(searchString));
    }

    @Benchmark
    public void stringMatchesRegex(Blackhole bh) {
        bh.consume(testString.matches(".*" + searchString + ".*"));
    }

    @Benchmark
    public void patternMatcher(Blackhole bh) {
        bh.consume(EMAIL_PATTERN.matcher("user@example.com").matches());
    }

    @Benchmark
    public void stringIntern(Blackhole bh) {
        String a = new String("hello");
        String b = new String("hello");
        bh.consume(a.intern() == b.intern());
    }

    @Benchmark
    public void stringEquals(Blackhole bh) {
        String a = new String("hello");
        String b = new String("hello");
        bh.consume(a.equals(b));
    }

    @Benchmark
    public void stringInternLookup(Blackhole bh) {
        String s = words[0].intern();
        for (int i = 1; i < 50; i++) bh.consume(s == words[i].intern());
    }

    @Benchmark
    public void stringSubstring(Blackhole bh) {
        for (int i = 0; i < count; i++) bh.consume(testString.substring(0, i));
    }

    @Benchmark
    public void stringSplit(Blackhole bh) {
        String[] parts = testString.split(" ");
        bh.consume(parts.length);
    }
}
