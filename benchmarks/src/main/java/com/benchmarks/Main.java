package com.benchmarks;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class Main {

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include("com\\.benchmarks\\..*Benchmark.*")
                .exclude("com\\.benchmarks\\.Main")
                .warmupIterations(3)
                .warmupTime(TimeValue.seconds(2))
                .measurementIterations(5)
                .measurementTime(TimeValue.seconds(3))
                .forks(2)
                .shouldDoGC(true)
                .shouldFailOnError(true)
                .jvmArgs("-Xms2g", "-Xmx2g")
                .result("jmh-results.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
