package com.benchmarks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Fork(value = 2, warmups = 1)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
public class SerializationBenchmark {

    @Param({"100", "1000"})
    private int size;

    private Gson gson;
    private ObjectMapper jackson;
    private UserData data;
    private byte[] javaSerialized;
    private String gsonSerialized;
    private String jacksonSerialized;

    @Setup
    public void setup() {
        gson = new Gson();
        jackson = new ObjectMapper();

        String[] names = {"Alice", "Bob", "Charlie", "Diana", "Eve"};
        int[] scores = new int[size];
        String[] tags = new String[size];
        for (int i = 0; i < size; i++) {
            scores[i] = (int) (Math.random() * 100);
            tags[i] = "tag-" + i;
        }
        data = new UserData(42, "Benchmark User", "user@example.com", true, scores, tags);

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(data);
            oos.flush();
            javaSerialized = bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gsonSerialized = gson.toJson(data);
        try {
            jacksonSerialized = jackson.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Benchmark
    public void javaSerialize(Blackhole bh) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(data);
        oos.flush();
        bh.consume(bos.toByteArray());
    }

    @Benchmark
    public void javaDeserialize(Blackhole bh) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(javaSerialized);
        ObjectInputStream ois = new ObjectInputStream(bis);
        bh.consume(ois.readObject());
    }

    @Benchmark
    public void gsonSerialize(Blackhole bh) {
        bh.consume(gson.toJson(data));
    }

    @Benchmark
    public void gsonDeserialize(Blackhole bh) {
        bh.consume(gson.fromJson(gsonSerialized, UserData.class));
    }

    @Benchmark
    public void jacksonSerialize(Blackhole bh) throws Exception {
        bh.consume(jackson.writeValueAsString(data));
    }

    @Benchmark
    public void jacksonDeserialize(Blackhole bh) throws Exception {
        bh.consume(jackson.readValue(jacksonSerialized, UserData.class));
    }

    @Benchmark
    public void javaSerializedSize(Blackhole bh) {
        bh.consume(javaSerialized.length);
    }

    @Benchmark
    public void gsonSerializedSize(Blackhole bh) {
        bh.consume(gsonSerialized.length());
    }

    @Benchmark
    public void jacksonSerializedSize(Blackhole bh) {
        bh.consume(jacksonSerialized.length());
    }

    static class UserData implements Serializable {
        private static final long serialVersionUID = 1L;
        private int id;
        private String name;
        private String email;
        private boolean active;
        private int[] scores;
        private String[] tags;

        public UserData() {}

        public UserData(int id, String name, String email, boolean active, int[] scores, String[] tags) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.active = active;
            this.scores = scores;
            this.tags = tags;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public int[] getScores() { return scores; }
        public void setScores(int[] scores) { this.scores = scores; }
        public String[] getTags() { return tags; }
        public void setTags(String[] tags) { this.tags = tags; }
    }
}
