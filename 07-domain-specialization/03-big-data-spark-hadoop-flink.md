# Domain Specialization — Lesson 3: Big Data (Spark, Hadoop, Flink)

> **INTRODUCTORY OVERVIEW** — This section provides a high-level introduction to the domain. Each topic warrants its own dedicated course for professional mastery.

## Why Big Data?

Traditional databases struggle when data exceeds terabytes or requires complex processing across thousands of machines. Big Data tools enable **distributed computing** — processing data across clusters of computers.

```
Traditional:                           Big Data:
┌──────────┐                           ┌──────────────────────────┐
│ One big  │                           │ Cluster of 100 machines   │
│ server   │                           │                          │
│          │                           │ ┌────┐ ┌────┐ ┌────┐   │
│ 1 TB RAM │                           │ │Node│ │Node│ │Node│...│
│ 100 TB   │                           │ │ 1  │ │ 2  │ │ 3  │   │
│ storage  │                           │ └────┘ └────┘ └────┘   │
│          │                           │                          │
│ $100K+   │                           │ $5K each = $500K         │
│          │                           │ (but SCALES to petabytes)│
│ MAX: 1PB │                           │ MAX: unlimited           │
└──────────┘                           └──────────────────────────┘
```

## Apache Hadoop

Hadoop provides **HDFS** (storage) + **MapReduce** (processing).

```
┌─────────────────────────────────────────────────────────────┐
│                     HADOOP CLUSTER                            │
│                                                               │
│  ┌──────────────────────────────────────────────────────────┐│
│  │                    HDFS (Storage)                         ││
│  │  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐        ││
│  │  │NameNode│  │DataNode│  │DataNode│  │DataNode│ ...     ││
│  │  │(master)│  │ 1      │  │ 2      │  │ 3      │        ││
│  │  └────────┘  └────────┘  └────────┘  └────────┘        ││
│  └──────────────────────────────────────────────────────────┘│
│                                                               │
│  ┌──────────────────────────────────────────────────────────┐│
│  │               YARN (Resource Management)                 ││
│  │  ┌────────┐  ┌────────┐  ┌────────┐                     ││
│  │  │Resource│  │Node-   │  │Node-   │                     ││
│  │  │Manager │  │Manager1│  │Manager2│ ...                 ││
│  │  └────────┘  └────────┘  └────────┘                     ││
│  └──────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

### Hadoop Streaming with Java

```java
// Mapper
public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private static final IntWritable ONE = new IntWritable(1);
    private Text word = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        String[] words = value.toString().split("\\s+");
        for (String w : words) {
            word.set(w.toLowerCase());
            context.write(word, ONE);
        }
    }
}

// Reducer
public class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
        int sum = 0;
        for (IntWritable val : values) {
            sum += val.get();
        }
        context.write(key, new IntWritable(sum));
    }
}
```

## Apache Spark

Spark is **100x faster** than Hadoop MapReduce because it processes data **in-memory** (instead of writing to disk between steps).

```
Hadoop MapReduce:                    Spark:
Read from HDFS ──▶ Map ──▶ DISK ──▶ Read from HDFS ──▶ Map ──▶ Reduce ──▶ Write
                    │        │        Reduce ──▶ Write    │
                    DISK ◀───┘        (all in MEMORY)     │
                                                            (no disk between stages)
   ❌ Disk I/O between every stage      ✅ In-memory processing
```

### Spark with Java

```xml
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-core_2.13</artifactId>
    <version>3.5.0</version>
</dependency>
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-sql_2.13</artifactId>
    <version>3.5.0</version>
</dependency>
```

### RDD (Resilient Distributed Dataset)

```java
SparkConf conf = new SparkConf().setAppName("WordCount").setMaster("local[*]");
JavaSparkContext sc = new JavaSparkContext(conf);

JavaRDD<String> lines = sc.textFile("hdfs:///data/input.txt");

JavaRDD<String> words = lines.flatMap(line -> Arrays.asList(line.split("\\s+")).iterator());

JavaPairRDD<String, Integer> pairs = words.mapToPair(word -> new Tuple2<>(word, 1));

JavaPairRDD<String, Integer> counts = pairs.reduceByKey(Integer::sum);

counts.saveAsTextFile("hdfs:///data/output/");
```

### DataFrame (Structured API)

```java
SparkSession spark = SparkSession.builder()
    .appName("SalesAnalysis")
    .master("local[*]")
    .getOrCreate();

Dataset<Row> sales = spark.read()
    .option("header", "true")
    .csv("hdfs:///data/sales.csv");

sales.createOrReplaceTempView("sales");

Dataset<Row> result = spark.sql("""
    SELECT category,
           SUM(amount) as total_sales,
           COUNT(*) as transaction_count
    FROM sales
    WHERE year = 2026
    GROUP BY category
    ORDER BY total_sales DESC
""");

result.show();
```

### Streaming

```java
Dataset<Row> stream = spark
    .readStream()
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "orders")
    .load();

Dataset<Row> salesByMinute = stream
    .selectExpr("CAST(value AS STRING)")
    .select(functions.from_json(
        functions.col("value"),
        "order_id STRING, amount DOUBLE, timestamp LONG"
    ).alias("data"))
    .select("data.*")
    .withWatermark("timestamp", "10 minutes")
    .groupBy(
        functions.window(functions.col("timestamp"), "5 minutes"),
        functions.col("order_id")
    )
    .agg(functions.sum("amount").alias("total"));

salesByMinute
    .writeStream()
    .outputMode("update")
    .format("console")
    .start()
    .awaitTermination();
```

## Apache Flink

Flink is designed for **real-time stream processing** with exactly-once semantics and low latency.

```
Batch Processing (Spark):              Stream Processing (Flink):
┌──────────────────────┐               ┌──────────────────────┐
│ ┌────┐ ┌────┐ ┌────┐ │               │ ───▶───▶───▶───▶───▶  │
│ │  1 │ │  2 │ │  3 │ │               │ Data flows CONTINUOUSLY│
│ │hour│ │hour│ │hour│ │               │                        │
│ └────┘ └────┘ └────┘ │               │ Process each event as │
│ Process in BATCHES   │               │ it arrives (5ms latency)│
└──────────────────────┘               └──────────────────────┘
```

### Flink with Java

```xml
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-streaming-java</artifactId>
    <version>1.18.0</version>
</dependency>
<dependency>
    <groupId>org.apache.flink</groupId>
    <artifactId>flink-clients</artifactId>
    <version>1.18.0</version>
</dependency>
```

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

DataStream<String> stream = env
    .addSource(new FlinkKafkaConsumer<>("orders",
        new SimpleStringSchema(),
        properties));

DataStream<Order> orders = stream
    .map(json -> new ObjectMapper().readValue(json, Order.class));

DataStream<OrderSummary> summaries = orders
    .keyBy(Order::getCategory)
    .window(TumblingProcessingTimeWindows.of(Time.minutes(5)))
    .aggregate(new OrderAggregator());

summaries.addSink(new FlinkKafkaProducer<>("order-summaries",
    new SimpleStringSchema(), properties));

env.execute("Order Processing Job");
```

## When to Use Which

| Tool | Best For | Latency | Language |
|------|----------|---------|----------|
| **Hadoop MapReduce** | Batch processing on existing HDFS, legacy systems | Minutes-Hours | Java |
| **Spark** | Fast batch, SQL analytics, streaming, ML | Seconds-Minutes | Java, Scala, Python, SQL |
| **Flink** | Real-time streaming, event-driven apps | Milliseconds | Java, Scala, Python |
| **Kafka Streams** | Lightweight streaming within microservices | Milliseconds | Java |

## Exercises

1. Set up a local Spark environment and run a word count on a text file.
2. Use Spark SQL to analyze a CSV dataset with aggregations and filters.
3. Create a Spark Streaming job that reads from a Kafka topic.
4. Write a Flink job that processes a stream of events with a tumbling window.
5. Compare the performance of Hadoop MapReduce vs Spark for a similar task.
