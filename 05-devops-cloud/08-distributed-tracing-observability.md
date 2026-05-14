# DevOps & Cloud — Lesson 8: Distributed Tracing & Observability

## Why Distributed Tracing?

In a monolith, a single log file shows the full request flow. In microservices, a single request spans 5+ services across 10+ different log files. **Distributed tracing** connects the dots.

```
Request flow across services:

┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ API      │     │ User     │     │ Order    │     │ Payment  │
│ Gateway  │────▶│ Service  │────▶│ Service  │────▶│ Service  │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
      │               │               │               │
      ▼               ▼               ▼               ▼
  Trace ID: abc123 (propagated via HTTP headers)
  Span 1: Gateway          (2ms)
  Span 2:   User Service   (5ms)
  Span 3:     Order Service (10ms)
  Span 4:       Payment    (3ms)
  ├──────────────────────────────────────────┤
  Total: 20ms (but ORDER service was the bottleneck!)
```

## Traces, Spans, and Context

```
TRACE: abc123
├── Span: HTTP GET /api/orders (root)
│   ├── Span: Auth validation (2ms)
│   │   ├── Span: DB query: find user (1ms)
│   │   └── Span: JWT verify (1ms)
│   ├── Span: Fetch orders (15ms)
│   │   ├── Span: HTTP call to order-service (12ms)
│   │   │   ├── Span: DB query: find orders (8ms)
│   │   │   └── Span: Cache check (4ms)
│   │   └── Span: Serialize response (3ms)
│   └── Span: Response formatting (3ms)
└── Total: 20ms

Key Insight: Fetch orders took 15/20ms = 75% of total!
```

| Concept | Description |
|---------|-------------|
| **Trace** | End-to-end representation of a single request |
| **Span** | A single unit of work within a trace |
| **Span Context** | Trace ID, Span ID, parent ID — propagated across services |
| **Parent Span** | The caller's span |
| **Child Span** | A sub-operation within a span |

## OpenTelemetry — The Standard

OpenTelemetry is the industry standard for generating, collecting, and exporting telemetry data (traces, metrics, logs).

```
┌─────────────────────────────────────────────────────────────┐
│                    OPENTELEMETRY                              │
│                                                               │
│  Your App ──▶ OpenTelemetry SDK ──▶ OTel Collector ──▶ Backend │
│  (Java)         (auto-instrument)     (agent)      │         │
│                                                     │         │
│                                          ┌──────────┼──────┐  │
│                                          ▼          ▼      ▼  │
│                                      Jaeger   Zipkin   Tempo  │
│                                      (traces)  (traces)(traces)│
│                                      Prometheus  CloudWatch   │
│                                      (metrics)   (traces)     │
└─────────────────────────────────────────────────────────────┘
```

### Java Agent (Auto-Instrumentation)

The easiest way to add tracing — attach the OTel Java agent:

```bash
java -javaagent:opentelemetry-javaagent.jar \
  -Dotel.service.name=user-service \
  -Dotel.traces.exporter=zipkin \
  -Dotel.exporter.zipkin.endpoint=http://zipkin:9411/api/v2/spans \
  -jar user-service.jar
```

This auto-instruments:
- HTTP requests (Servlet, Spring Web, Netty)
- Database calls (JDBC, R2DBC)
- Messaging (Kafka, RabbitMQ, JMS)
- gRPC, GraphQL, AWS SDK, Redis, and more

### Manual Instrumentation

```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-api</artifactId>
    <version>1.31.0</version>
</dependency>
```

```java
@Service
public class OrderService {

    @Autowired
    private Tracer tracer;

    public Order createOrder(OrderRequest request) {
        // Create a custom span
        Span span = tracer.spanBuilder("createOrder")
            .setAttribute("user.id", request.getUserId())
            .setAttribute("order.total", request.getTotal().doubleValue())
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Business logic
            validateInventory(request);
            Order order = saveOrder(request);
            processPayment(order);

            span.setStatus(StatusCode.OK);
            return order;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR);
            throw e;
        } finally {
            span.end();
        }
    }
}
```

## Spring Boot with Micrometer Tracing

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

```properties
# All you need — auto-configures tracing
management.tracing.sampling.probability=1.0
```

```java
@Service
public class TracingService {

    @Autowired
    private Tracer tracer;  // Micrometer's Tracer (wraps OpenTelemetry)

    @Autowired
    private ObservationRegistry observationRegistry;

    public void doWork() {
        // Automatic — every HTTP request, DB call, etc. is traced

        // Manual observation
        Observation.createNotStarted("custom.operation", observationRegistry)
            .lowCardinalityKeyValue("service", "user-service")
            .observe(() -> {
                // Your traced code here
                Thread.sleep(100);
            });
    }
}
```

### Propagating Trace Context

For inter-service HTTP calls using WebClient:

```java
@Bean
public WebClient webClient(ObservationRegistry registry, Tracer tracer) {
    return WebClient.builder()
        .filter(ObservationWebClientCustomizer.builder(registry, tracer)
            .build()
            .customize())
        .build();
}
```

For RestTemplate:

```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}

// Trace context is propagated via HTTP headers:
// traceparent: 00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01
```

## Zipkin — Trace Visualization

```yaml
# docker-compose for Zipkin
services:
  zipkin:
    image: openzipkin/zipkin:latest
    ports:
      - "9411:9411"
```

### Viewing Traces

```
Zipkin UI (http://localhost:9411):

┌─────────────────────────────────────────────────────────────┐
│  Trace: abc123  Duration: 245ms  Spans: 7                   │
│                                                               │
│  Service          Operation               Duration    Tags   │
│  ─────────────────────────────────────────────────────────   │
│  api-gateway      /api/orders              245ms      ▲      │
│  ├─ user-service  validateToken             12ms      │      │
│  ├─ order-service getOrders                 198ms     │      │
│  │  ├─ postgres   SELECT * FROM orders      150ms    BOTTLENECK!
│  │  └─ redis      GET cached_orders          48ms      │      │
│  └─ user-service  enrichUserData            35ms       │      │
└─────────────────────────────────────────────────────────────┘
```

## Jaeger — Alternative Trace Backend

```yaml
services:
  jaeger:
    image: jaegertracing/all-in-one:latest
    environment:
      - COLLECTOR_OTLP_ENABLED=true
    ports:
      - "16686:16686"  # UI
      - "4317:4317"    # OTLP gRPC
```

## Grafana Tempo — Scalable Trace Storage

For production-scale tracing, use Grafana Tempo:

```yaml
# tempo.yml
server:
  http_listen_port: 3200

distributor:
  receivers:
    otlp:
      protocols:
        grpc:

ingester:
  trace_idle_period: 10s

compactor:
  compaction:
    block_retention: 48h

storage:
  trace:
    backend: s3
    s3:
      bucket: myapp-traces
      endpoint: s3.us-east-1.amazonaws.com
```

## Correlation — Traces + Logs + Metrics

The real power: correlate traces with logs and metrics.

```xml
<!-- logback-spring.xml — Add trace IDs to logs -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>spanId</includeMdcKeyName>
        </encoder>
    </appender>
</configuration>
```

Your logs will include trace IDs automatically:

```json
{
  "@timestamp": "2026-05-12T10:30:00.000Z",
  "level": "ERROR",
  "logger": "com.example.OrderService",
  "message": "Payment failed",
  "traceId": "0af7651916cd43dd8448eb211c80319c",
  "spanId": "b7ad6b7169203331"
}
```

Now you can: **See ERROR in logs → Click trace ID → See full request flow → Find root cause.**

## OpenTelemetry SDK Setup for Java

### Auto-Instrumentation (Recommended)

```bash
# Download the OTel Java agent
wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# Run with auto-instrumentation
java -javaagent:opentelemetry-javaagent.jar \
  -Dotel.service.name=user-service \
  -Dotel.traces.exporter=otlp \
  -Dotel.metrics.exporter=otlp \
  -Dotel.logs.exporter=otlp \
  -Dotel.exporter.otlp.endpoint=http://otel-collector:4317 \
  -Dotel.resource.attributes=deployment.environment=production,service.version=1.2.0 \
  -jar user-service.jar
```

```yaml
# docker-compose for OTel Collector
services:
  otel-collector:
    image: otel/opentelemetry-collector-contrib:latest
    command: ["--config=/etc/otel-collector-config.yaml"]
    volumes:
      - ./otel-collector-config.yaml:/etc/otel-collector-config.yaml
    ports:
      - "4317:4317"   # OTLP gRPC
      - "4318:4318"   # OTLP HTTP
      - "8888:8888"   # Prometheus metrics
```

```yaml
# otel-collector-config.yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

processors:
  batch:
    timeout: 1s
    send_batch_size: 1024
  memory_limiter:
    check_interval: 1s
    limit_mib: 512
    spike_limit_mib: 128
  attributes:
    actions:
      - key: environment
        value: production
        action: insert

exporters:
  prometheus:
    endpoint: "0.0.0.0:8889"
    namespace: java_app
  otlp:
    endpoint: "jaeger:4317"
    tls:
      insecure: true
  debug:
    verbosity: detailed

service:
  pipelines:
    traces:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [otlp, debug]
    metrics:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [prometheus, debug]
    logs:
      receivers: [otlp]
      processors: [memory_limiter, batch]
      exporters: [debug]
```

### Manual Spans with OpenTelemetry SDK

```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-api</artifactId>
    <version>1.31.0</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk</artifactId>
    <version>1.31.0</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
    <version>1.31.0</version>
</dependency>
```

```java
@Configuration
public class OpenTelemetryConfig {

    @Bean
    public OpenTelemetry openTelemetry() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(
                OtlpGrpcSpanExporter.builder()
                    .setEndpoint("http://otel-collector:4317")
                    .build())
                .build())
            .setResource(Resource.getDefault()
                .toBuilder()
                .put("service.name", "user-service")
                .put("service.version", "1.2.0")
                .put("deployment.environment", "production")
                .build())
            .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setLoggerProvider(
                SdkLoggerProvider.builder()
                    .addLogRecordProcessor(
                        BatchLogRecordProcessor.builder(
                            OtlpGrpcLogRecordExporter.builder()
                                .setEndpoint("http://otel-collector:4317")
                                .build())
                            .build())
                    .build())
            .buildAndRegister();
    }
}
```

```java
@Service
public class PaymentService {

    private final Tracer tracer;

    public PaymentService(OpenTelemetry openTelemetry) {
        this.tracer = openTelemetry.getTracer("payment-service");
    }

    public PaymentResult processPayment(PaymentRequest request) {
        Span span = tracer.spanBuilder("processPayment")
            .setSpanKind(SpanKind.CLIENT)
            .setAttribute("payment.amount", request.amount())
            .setAttribute("payment.currency", request.currency())
            .setAttribute("payment.method", request.method())
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // Add events within the span
            span.addEvent("Validating payment...", Attributes.of(
                AttributeKey.stringKey("payment.id"), request.id()
            ));

            PaymentResult result = paymentGateway.charge(request);

            span.addEvent("Payment completed", Attributes.of(
                AttributeKey.stringKey("transaction.id"), result.transactionId()
            ));

            span.setStatus(StatusCode.OK);
            return result;

        } catch (PaymentException e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Payment failed: " + e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

### Context Propagation Across Services

```java
// REST template with OpenTelemetry propagation
@Bean
public RestTemplate restTemplate(OpenTelemetry openTelemetry) {
    RestTemplate template = new RestTemplate();
    template.getInterceptors().add((request, body, execution) -> {
        // Inject current span context into outgoing request headers
        TextMapSetter<HttpHeaders> setter =
            (carrier, key, value) -> carrier.set(key, value);
        openTelemetry.getPropagators()
            .getTextMapPropagator()
            .inject(Context.current(), request.getHeaders(), setter);
        return execution.execute(request, body);
    });
    return template;
}
```

## Three Pillars of Observability — Correlation

```java
// Correlating metrics, logs, and traces

// 1. Metrics — recorded via Micrometer
Timer.Sample sample = Timer.start(registry);
try {
    // Business logic
    sample.stop(Timer.builder("order.processing.time")
        .tag("status", "success")
        .register(registry));
} catch (Exception e) {
    sample.stop(Timer.builder("order.processing.time")
        .tag("status", "failure")
        .register(registry));
}

// 2. Logs — with trace context
// MDC automatically populated by Micrometer Tracing observation
Logger log = LoggerFactory.getLogger(OrderService.class);
// Trace ID, Span ID automatically populated in MDC
// Log: {"traceId": "abc123", "spanId": "def456", "message": "Order created"}

// 3. Traces — captured via OTel agent or Micrometer Tracing

// To correlate all three:
// - Use same trace_id in logs and traces
// - Use Micrometer Observation API to automatically create metrics + traces
Observation.createNotStarted("order.created", registry)
    .contextualName("create-order")
    .lowCardinalityKeyValue("order.type", request.type())
    .observe(() -> orderService.create(request));
// This automatically:
// - Creates a span named "order.created"
// - Records timer metric "order.created"
// - Populates MDC with traceId/spanId for logs
```

## Micrometer Metrics in Spring Boot — Complete Example

```java
@Configuration
public class MicrometerConfig {

    // Custom meter binder
    @Bean
    public MeterBinder queueMetrics(QueueService queueService) {
        return registry -> {
            Gauge.builder("queue.pending", queueService, QueueService::pendingCount)
                .description("Pending items in queue")
                .tag("queue", "orders")
                .register(registry);

            Gauge.builder("queue.processing", queueService, QueueService::processingCount)
                .description("Currently processing items")
                .tag("queue", "orders")
                .register(registry);
        };
    }
}

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final MeterRegistry registry;
    private final Counter orderCounter;

    public OrderController(MeterRegistry registry) {
        this.registry = registry;
        this.orderCounter = Counter.builder("orders.api.requests")
            .description("Total API requests to order endpoints")
            .register(registry);
    }

    @GetMapping
    public List<Order> getOrders() {
        orderCounter.increment();
        return orderService.findAll();
    }

    @PostMapping
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        return Timer.builder("orders.create.time")
            .publishPercentiles(0.95, 0.99)
            .sla(Duration.ofMillis(100), Duration.ofMillis(500))
            .register(registry)
            .record(() -> orderService.create(request));
    }
}
```

### Spring Boot Actuator Metrics Endpoints

```properties
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,env,loggers,threaddump,heapdump
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
    metrics:
      enabled: true
    prometheus:
      enabled: true
  metrics:
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      slas:
        http.server.requests: 100ms, 500ms, 1s, 2s
```

## Jaeger vs Zipkin Comparison

| Feature | Jaeger | Zipkin |
|---------|--------|--------|
| **Origin** | Uber (CNCF) | Twitter (OpenZipkin) |
| **Storage** | Elasticsearch, Cassandra, Kafka, Badger | Elasticsearch, Cassandra, MySQL, S3 |
| **UI** | Richer UI with service graph, deep dependencies | Simpler, clean UI |
| **Sampling** | Adaptive, probabilistic, rate-limiting | Probabilistic, rate-limiting |
| **Protocol** | Jaeger native, OTLP (via collector) | Zipkin JSON/Thrift, OTLP (via collector) |
| **gRPC** | Native gRPC support | Via collector |
| **Kubernetes** | Jaeger Operator available | Zipkin Operator available |
| **Scalability** | Designed for high volume | Good, but simpler architecture |
| **Monitoring** | Built-in service dependencies graph | Plugin-based |

```yaml
# Jaeger all-in-one (dev)
services:
  jaeger:
    image: jaegertracing/all-in-one:latest
    environment:
      - COLLECTOR_OTLP_ENABLED=true
      - COLLECTOR_ZIPKIN_HOST_PORT=:9411
    ports:
      - "16686:16686"  # UI
      - "4317:4317"    # OTLP gRPC
      - "4318:4318"    # OTLP HTTP
      - "9411:9411"    # Zipkin compatible

# Zipkin all-in-one (dev)
services:
  zipkin:
    image: openzipkin/zipkin:latest
    environment:
      - STORAGE_TYPE=mem
    ports:
      - "9411:9411"
```

## SLOs, SLIs, and Error Budgets

```java
// SLI (Service Level Indicator): measured metric
// SLO (Service Level Objective): target threshold
// Error Budget: allowed failure = 100% - SLO%

// Example: API availability SLI
// SLI: proportion of successful requests
// SLO: 99.9% availability (monthly)
// Error Budget: 0.1% = ~43 minutes downtime/month

@Component
public class SloMonitor {

    private final MeterRegistry registry;

    // Track SLO compliance
    public void recordRequest(int statusCode) {
        boolean success = statusCode < 500;
        registry.counter("slo.requests.total").increment();
        if (success) {
            registry.counter("slo.requests.successful").increment();
        } else {
            registry.counter("slo.requests.failed").increment();
        }
    }

    // Calculate current burn rate
    public double getErrorBudgetBurnRate() {
        double total = registry.counter("slo.requests.total").count();
        double failed = registry.counter("slo.requests.failed").count();
        if (total == 0) return 0;
        double errorRate = failed / total;
        double slo = 0.999;  // 99.9%
        double budgetConsumed = errorRate / (1 - slo);
        return budgetConsumed * 100;
    }
}
```

```promql
# Prometheus SLO queries

# SLI: proportion of successful requests (30d window)
sum(rate(http_server_requests_seconds_count{status!~"5.."}[30d]))
  / sum(rate(http_server_requests_seconds_count[30d]))

# Error budget remaining (percentage)
(
  1 - (
    sum(rate(http_server_requests_seconds_count{status=~"5.."}[30d]))
    / sum(rate(http_server_requests_seconds_count[30d]))
  )
) / (1 - 0.999) * 100

# Burn rate over 1h (alert if > 1 = exceeding budget)
(
  sum(rate(http_server_requests_seconds_count{status=~"5.."}[1h]))
  / sum(rate(http_server_requests_seconds_count[1h]))
) / (1 - 0.999)

# Multi-window, multi-burn-rate alert
# Alert if burn rate > 3x for 1h OR 1x for 6h
```

## GOLD Signals (Latency, Traffic, Errors, Saturation)

### Google's Four Golden Signals for Microservices

```promql
# 1. LATENCY — Time to respond
# P99 latency over last 5 minutes
histogram_quantile(0.99,
  sum(rate(http_server_requests_seconds_bucket[5m])) by (le, service)
)

# 2. TRAFFIC — Demand on the system
# Total requests per second
sum(rate(http_server_requests_seconds_count[5m])) by (service)

# 3. ERRORS — Rate of failed requests
# HTTP 5xx rate as %
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (service)
  / sum(rate(http_server_requests_seconds_count[5m])) by (service)
  * 100

# 4. SATURATION — How "full" the service is
# CPU usage %
avg(rate(process_cpu_usage[5m])) by (service)
# Memory usage %
jvm_memory_used_bytes{area="heap"}
  / jvm_memory_max_bytes{area="heap"}
  * 100
# DB connection pool saturation
hikaricp_connections_active / hikaricp_connections_max * 100
```

### RED Method (Rate, Errors, Duration) — Alternative to GOLD

```
RATE:     Requests per second
ERRORS:   Number of failed requests per second
DURATION: Distribution of response time

Simpler than GOLD: focuses on request-level metrics only.
Best for microservices monitoring.
```

```promql
# RED metrics for a single service

# Rate
sum(rate(http_server_requests_seconds_count{application="user-service"}[5m]))

# Errors
sum(rate(http_server_requests_seconds_count{application="user-service",status=~"5.."}[5m]))

# Duration (P99)
histogram_quantile(0.99,
  sum(rate(http_server_requests_seconds_bucket{application="user-service"}[5m])) by (le)
)
```

## Observability Maturity Model

| Level | What You Have | Can Answer |
|-------|---------------|------------|
| 1 — Black Box | Is it up? (ping) | Is the server running? |
| 2 — Basic Metrics | CPU, memory, disk | Is it healthy right now? |
| 3 — Application Metrics | Request rate, error rate, latency | Is performance okay? |
| 4 — Centralized Logs | All logs in one place | What errors are happening? |
| 5 — Distributed Tracing | End-to-end traces | Where is the bottleneck? |
| 6 — Correlated Observability | Traces + Logs + Metrics linked | Why did that error happen? |

## Exercises

1. Attach the OpenTelemetry Java agent to a Spring Boot app and send traces to Zipkin.
2. Create a custom span around a critical business operation.
3. View a trace in Zipkin and identify the slowest span.
4. Propagate trace context across two microservices using HTTP headers.
5. Configure log correlation so trace IDs appear in log messages.
