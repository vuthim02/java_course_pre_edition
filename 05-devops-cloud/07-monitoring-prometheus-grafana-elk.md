# DevOps & Cloud — Lesson 7: Monitoring (Prometheus, Grafana, ELK)

## Why Monitoring?

```
Without Monitoring:                    With Monitoring:
┌──────────────────────────────┐      ┌──────────────────────────────┐
│ "Is the app slow?"           │      │ Dashboard shows:             │
│ "I don't know, let me SSH"   │      │ • Response time: 95th %tile  │
│                              │      │ • Error rate: 0.02%          │
│ Customer: "Site is down!"   │      │ • CPU: 45%, Memory: 62%      │
│ Dev: "Works on my machine"  │      │ • Throughput: 1200 req/s     │
│                              │      │                              │
│ 😱 REACTIVE — find out      │      │ 🚀 PROACTIVE — alerts BEFORE │
│   when users complain       │      │   users notice               │
└──────────────────────────────┘      └──────────────────────────────┘
```

## The Three Pillars of Observability

```
┌─────────────────────────────────────────────────────────────┐
│                      OBSERVABILITY                            │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐    │
│  │   METRICS    │  │     LOGS     │  │      TRACES      │    │
│  │              │  │              │  │                  │    │
│  │ "What's      │  │ "What        │  │ "Where in the    │    │
│  │  happening?" │  │  happened?"  │  │  request flow?"  │    │
│  │              │  │              │  │                  │    │
│  │ CPU: 80%     │  │ ERROR: DB   │  │ UserService      │    │
│  │ Error: 5%    │  │ connection  │  │   → OrderService │    │
│  │ Req/s: 1000  │  │ timeout     │  │     → PostgreSQL  │    │
│  └──────────────┘  └──────────────┘  └──────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## Prometheus — Metrics Collection

Prometheus **scrapes** metrics from your applications at regular intervals.

```
┌─────────────────────────────────────────────────────────────┐
│                      PROMETHEUS                               │
│                                                               │
│  ┌──────────┐   scrape /actuator/prometheus    ┌──────────┐ │
│  │ Service  │◀─────────────────────────────────│Prometheus│ │
│  │ A:8080   │  (every 15s)                     │ Server   │ │
│  └──────────┘                                   │          │ │
│  ┌──────────┐   scrape /actuator/prometheus    │ Store in │ │
│  │ Service  │◀─────────────────────────────────│ TSDB     │ │
│  │ B:8081   │                                  └────┬─────┘ │
│  └──────────┘                                       │       │
│                                               query │       │
│                                                     ▼       │
│                                              ┌──────────┐   │
│                                              │ Grafana  │   │
│                                              │ (dash-   │   │
│                                              │ boards)  │   │
│                                              └──────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Spring Boot Actuator + Prometheus

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```properties
management.endpoints.web.exposure.include=health,metrics,prometheus
management.endpoint.health.show-details=always
management.metrics.export.prometheus.enabled=true
management.metrics.tags.application=${spring.application.name}
```

### Key Metrics Exposed

```
# Custom metrics
http_server_requests_seconds_count{method="GET",status="200"}  15234
http_server_requests_seconds_sum{method="GET",status="200"}  28.5
jvm_memory_used_bytes{area="heap"}  2.5E8
jvm_gc_pause_seconds_count  42
logback_events_total{level="error"}  7
```

### Custom Metrics

```java
@Component
public class OrderMetrics {

    private final Counter orderCreatedCounter;
    private final Timer orderProcessingTime;
    private final Gauge pendingOrdersGauge;

    public OrderMetrics(MeterRegistry registry) {
        orderCreatedCounter = Counter.builder("orders.created.total")
            .description("Total orders created")
            .register(registry);

        orderProcessingTime = Timer.builder("orders.processing.time")
            .description("Time to process an order")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(registry);

        pendingOrdersGauge = Gauge.builder("orders.pending", this,
            OrderMetrics::getPendingOrderCount)
            .register(registry);
    }

    public void recordOrderCreated() {
        orderCreatedCounter.increment();
    }

    public <T> T measureProcessingTime(Supplier<T> action) {
        return orderProcessingTime.record(action);
    }
}
```

### Prometheus Configuration

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'java-services'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - 'user-service:8080'
        - 'order-service:8081'
        - 'payment-service:8082'
    relabel_configs:
      - source_labels: [__address__]
        regex: '([^:]+):.*'
        target_label: instance
```

## Grafana — Visualization

Grafana turns Prometheus metrics into dashboards.

### Key Queries

```
# Request rate (per second)
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# P99 Response Time
histogram_quantile(0.99,
  rate(http_server_requests_seconds_bucket[5m]))

# Error rate percentage
(sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
  / sum(rate(http_server_requests_seconds_count[5m]))) * 100

# JVM Heap Usage
jvm_memory_used_bytes{area="heap"}

# GC Pause Rate
rate(jvm_gc_pause_seconds_count[5m])
```

### Dashboard Panels

| Panel | Query | Alert Threshold |
|-------|-------|-----------------|
| Error Rate | `error_rate_percentage` | > 1% for 5 min |
| P95 Latency | `histogram_quantile(0.95, ...)` | > 500ms |
| CPU Usage | `system_cpu_usage` | > 80% |
| Heap Memory | `jvm_memory_used_bytes{area="heap"}` | > 90% of max |
| Active Threads | `jvm_threads_live_threads` | > 200 |
| Database Connections | `hikaricp_connections_active` | > 80% of pool |

### Alerting

```yaml
# alerts.yml
groups:
  - name: java-service-alerts
    rules:
      - alert: HighErrorRate
        expr: |
          (sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
            / sum(rate(http_server_requests_seconds_count[5m]))) * 100 > 5
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Error rate > 5% for {{ $value | humanize }}"

      - alert: HighLatency
        expr: |
          histogram_quantile(0.95,
            rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "P95 latency > 1s"
```

## ELK Stack — Log Aggregation

```
┌──────────┐    ┌──────────┐    ┌──────────┐
│Filebeat   │───▶│Logstash  │───▶│Elastic-  │
│(log       │    │(parse &  │    │search    │
│ shipper)  │    │ transform)│   │(store &  │
└──────────┘    └──────────┘    │ index)   │
                                └────┬─────┘
                                     │
                                ┌────▼─────┐
                                │  Kibana   │
                                │(visualize │
                                │ & search) │
                                └──────────┘
```

### Docker Compose for ELK

```yaml
# docker-compose.yml
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - ES_JAVA_OPTS=-Xms1g -Xmx1g
    ports:
      - "9200:9200"

  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    ports:
      - "5000:5000"

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    ports:
      - "5601:5601"
```

### Logstash Configuration

```plaintext
# logstash.conf
input {
  tcp {
    port => 5000
    codec => json
  }
}

filter {
  grok {
    match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:level} \[%{DATA:thread}\] %{DATA:logger} - %{GREEDYDATA:log_message}" }
  }

  if [level] in ["ERROR", "WARN"] {
    mutate {
      add_tag => ["important"]
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "java-logs-%{+YYYY.MM.dd}"
  }
}
```

### Structured Logging (Logstash)

```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>localhost:5000</destination>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{ISO8601} %level [%thread] %logger - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="LOGSTASH"/>
    </root>
</configuration>
```

## Prometheus Metrics Types

```java
// Counter — monotonically increasing (requests, errors, tasks completed)
@Component
public class RequestMetrics {
    private final Counter requestTotal;
    private final Counter errorTotal;
    
    public RequestMetrics(MeterRegistry registry) {
        requestTotal = Counter.builder("http.requests.total")
            .description("Total HTTP requests")
            .tag("application", "user-service")
            .register(registry);
            
        errorTotal = Counter.builder("http.requests.errors")
            .description("Total HTTP request errors")
            .tag("application", "user-service")
            .register(registry);
    }
    
    public void recordRequest() { requestTotal.increment(); }
    public void recordError() { errorTotal.increment(); }
}

// Gauge — point-in-time value (queue size, active threads, memory usage)
Gauge gauge = Gauge.builder("queue.size", queue, Queue::size)
    .description("Current queue size")
    .register(registry);

// Timer — measures duration and frequency of events
Timer timer = Timer.builder("api.response.time")
    .description("API response time")
    .publishPercentiles(0.5, 0.95, 0.99)
    .publishPercentileHistogram()
    .sla(Duration.ofMillis(100), Duration.ofMillis(500), Duration.ofSeconds(1))
    .register(registry);

// Summary — similar to Timer but works with any unit (e.g., response payload size)
DistributionSummary summary = DistributionSummary.builder("response.payload.bytes")
    .description("Response payload size in bytes")
    .baseUnit("bytes")
    .publishPercentiles(0.5, 0.95, 0.99)
    .register(registry);
```

### Metric Naming Conventions

```
# Use snake_case, not camelCase
# Use dots as separators
# Use base unit in name

http.server.requests.total       ✓
http_server_requests_seconds     ✓  (Prometheus naming)
OrderService.getOrders()         ✗
```

## Exporters

```yaml
# Prometheus exporters for infrastructure monitoring

# node_exporter — OS-level metrics (CPU, memory, disk, network)
# Install on every node:
#   ./node_exporter --web.listen-address=:9100

  - job_name: 'node_exporter'
    static_configs:
      - targets:
        - 'node1:9100'
        - 'node2:9100'
        - 'node3:9100'

# blackbox_exporter — probing endpoints (HTTP, TCP, ICMP)
  - job_name: 'blackbox_http'
    metrics_path: /probe
    params:
      module: [http_2xx]
    static_configs:
      - targets:
        - https://api.myapp.com/actuator/health
        - https://myapp.com
    relabel_configs:
      - source_labels: [__address__]
        target_label: __param_target
      - source_labels: [__param_target]
        target_label: instance
      - target_label: __address__
        replacement: blackbox-exporter:9115

# JMX exporter — JVM metrics (for non-Spring Boot Java apps)
  - job_name: 'jmx_exporter'
    static_configs:
      - targets:
        - 'legacy-app:5556'   # JMX exporter agent port
```

```bash
# Run JMX exporter as Java agent
java -javaagent:jmx_prometheus_javaagent-0.20.0.jar=5556:config.yaml \
     -jar legacy-app.jar
```

```yaml
# config.yaml for JMX exporter
startDelaySeconds: 0
ssl: false
rules:
  - pattern: "java.lang<type=Memory><>HeapMemoryUsage"
    name: jvm_memory_heap_bytes
    type: GAUGE
  - pattern: "java.lang<type=Threading><>ThreadCount"
    name: jvm_threads_count
    type: GAUGE
```

## PromQL Query Examples

```promql
# Rate of requests per second
rate(http_server_requests_seconds_count[5m])

# Rate of 5xx errors
rate(http_server_requests_seconds_count{status=~"5.."}[5m])

# Error ratio as percentage
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
  / sum(rate(http_server_requests_seconds_count[5m]))
  * 100

# P95 latency (from histogram)
histogram_quantile(0.95,
  sum(rate(http_server_requests_seconds_bucket[5m])) by (le))

# P99 latency by endpoint
histogram_quantile(0.99,
  sum(rate(http_server_requests_seconds_bucket{uri="/api/orders"}[5m])) by (le))

# Increasing latency over time (derivative)
deriv(http_server_requests_seconds_sum{status="200"}[15m])

# JVM heap usage %
100 * (jvm_memory_used_bytes{area="heap"}
  / jvm_memory_max_bytes{area="heap"})

# GC pause time per minute
increase(jvm_gc_pause_seconds_sum[1m])

# Active database connections
hikaricp_connections_active / hikaricp_connections_max * 100

# Pod CPU usage in Kubernetes
sum(rate(container_cpu_usage_seconds_total{container!=""}[5m])) by (pod)

# Memory saturation threshold
node_memory_MemTotal_bytes - node_memory_MemFree_bytes - node_memory_Buffers_bytes
  - node_memory_Cached_bytes

# Predict disk fill in 24 hours
predict_linear(node_filesystem_free_bytes{mountpoint="/"}[6h], 86400)

# Average request duration per URI
rate(http_server_requests_seconds_sum[5m])
  / rate(http_server_requests_seconds_count[5m])

# Top 5 slowest endpoints
topk(5,
  rate(http_server_requests_seconds_sum[5m])
    / rate(http_server_requests_seconds_count[5m])
)
```

## Grafana Dashboard Creation

```json
// Grafana dashboard JSON model (simplified)
{
  "title": "Java Microservice Dashboard",
  "uid": "java-service-dashboard",
  "panels": [
    {
      "title": "Request Rate",
      "type": "timeseries",
      "datasource": "Prometheus",
      "targets": [{
        "expr": "sum(rate(http_server_requests_seconds_count[5m]))",
        "legendFormat": "Total Requests"
      }],
      "unit": "req/s",
      "gridPos": {"h": 8, "w": 8, "x": 0, "y": 0}
    },
    {
      "title": "Error Rate %",
      "type": "timeseries",
      "datasource": "Prometheus",
      "targets": [{
        "expr": "sum(rate(http_server_requests_seconds_count{status=~\"5..\"}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) * 100",
        "legendFormat": "Error %"
      }],
      "unit": "percent",
      "thresholds": [
        {"value": 1, "color": "yellow"},
        {"value": 5, "color": "red"}
      ],
      "gridPos": {"h": 8, "w": 8, "x": 8, "y": 0}
    },
    {
      "title": "P95/P99 Latency",
      "type": "timeseries",
      "datasource": "Prometheus",
      "targets": [
        {
          "expr": "histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))",
          "legendFormat": "P95"
        },
        {
          "expr": "histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))",
          "legendFormat": "P99"
        }
      ],
      "unit": "s",
      "gridPos": {"h": 8, "w": 8, "x": 16, "y": 0}
    },
    {
      "title": "Heap Memory",
      "type": "timeseries",
      "datasource": "Prometheus",
      "targets": [{
        "expr": "jvm_memory_used_bytes{area=\"heap\"}",
        "legendFormat": "Used"
      }],
      "unit": "bytes",
      "gridPos": {"h": 8, "w": 12, "x": 0, "y": 8}
    }
  ],
  "templating": {
    "list": [
      {
        "name": "instance",
        "type": "query",
        "query": "label_values(http_server_requests_seconds_count, instance)"
      },
      {
        "name": "uri",
        "type": "query",
        "query": "label_values(http_server_requests_seconds_count{instance=~\"$instance\"}, uri)"
      }
    ]
  },
  "annotations": {
    "list": [{
      "name": "Deployments",
      "datasource": "Prometheus",
      "expr": "timestamp(changes(http_server_requests_seconds_count[1m]) > 100)"
    }]
  }
}
```

### Grafana Variables

```sql
-- Template variables for dynamic dashboards

-- Query variable: list of instances
label_values(http_server_requests_seconds_count, instance)

-- Query variable: URIs filtered by selected instance
label_values(http_server_requests_seconds_count{instance=~"$instance"}, uri)

-- Custom variable
Values: prod, staging, dev

-- Interval variable
Values: 1m,5m,15m,30m,1h,6h,12h,24h
```

### Grafana Alerts

```yaml
# Grafana alert rule (Grafana 8+ unified alerting)
apiVersion: 1
groups:
  - name: java-service-alerts
    interval: 30s
    rules:
      - uid: high_error_rate
        title: "High Error Rate"
        condition: "A"
        data:
          - refId: "A"
            datasourceUid: "prometheus"
            model:
              expr: "sum(rate(http_server_requests_seconds_count{status=~\"5..\"}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) * 100 > 5"
              intervalMs: 30000
              maxDataPoints: 43200
        no_data_state: "NoData"
        exec_err_state: "Error"
        for: "5m"
        annotations:
          summary: "Error rate is {{ $values.A.Value }}% (threshold: 5%)"
          runbook_url: "https://wiki.myorg.com/runbooks/high-error-rate"
        labels:
          severity: "critical"
          team: "backend"
```

## ELK Stack — Complete Setup

```yaml
# docker-compose-elk.yml — Full ELK stack
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - cluster.name=java-logs
      - discovery.type=single-node
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms2g -Xmx2g"
      - xpack.security.enabled=false
      - xpack.monitoring.collection.enabled=true
    ulimits:
      memlock:
        soft: -1
        hard: -1
      nofile:
        soft: 65536
        hard: 65536
    volumes:
      - es-data:/usr/share/elasticsearch/data
    ports:
      - "9200:9200"
    networks:
      - elk

  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline
      - ./logstash/patterns:/usr/share/logstash/patterns
    ports:
      - "5000:5000/tcp"
      - "5000:5000/udp"
      - "9600:9600"  # Logstash monitoring API
    environment:
      - LS_JAVA_OPTS=-Xms1g -Xmx1g
    depends_on:
      - elasticsearch
    networks:
      - elk

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
      - SERVER_NAME=kibana
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
    networks:
      - elk

  filebeat:
    image: docker.elastic.co/beats/filebeat:8.11.0
    user: root
    volumes:
      - ./filebeat.yml:/usr/share/filebeat/filebeat.yml:ro
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    depends_on:
      - logstash
      - elasticsearch
    networks:
      - elk

volumes:
  es-data:

networks:
  elk:
```

### Filebeat Configuration

```yaml
# filebeat.yml — collect Docker container logs
filebeat.autodiscover:
  providers:
    - type: docker
      hints.enabled: true

filebeat.inputs:
  - type: container
    paths:
      - '/var/lib/docker/containers/*/*.log'

processors:
  - add_docker_metadata:
      host: "unix:///var/run/docker.sock"
  - decode_json_fields:
      fields: ["message"]
      target: "json"
      overwrite_keys: true
  - timestamp:
      field: json.@timestamp
      layouts:
        - "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        - "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
      test:
        - "2026-05-12T10:30:00.000Z"

output.logstash:
  hosts: ["logstash:5000"]
```

### Logstash Configuration with Filters

```plaintext
# logstash/pipeline/java-logs.conf
input {
  beats {
    port => 5044
  }
  tcp {
    port => 5000
    codec => json
  }
}

filter {
  # Parse structured JSON logs
  if [json][level] {
    mutate {
      copy => { "[json][level]" => "level" }
      copy => { "[json][logger]" => "logger" }
      copy => { "[json][message]" => "message" }
      copy => { "[json][traceId]" => "trace_id" }
      copy => { "[json][spanId]" => "span_id" }
    }
  }

  # Parse unstructured logs with grok
  if ![json][level] {
    grok {
      match => {
        "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:level} \[%{DATA:thread}\] %{DATA:logger} - %{GREEDYDATA:log_message}"
      }
    }
  }

  # Anonymize PII
  if [message] =~ /email/ {
    mutate {
      gsub => [
        "message", "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}", "[EMAIL REDACTED]"
      ]
    }
  }

  # Add geographic info from IP
  if [json][client_ip] {
    geoip {
      source => "[json][client_ip]"
      target => "geoip"
    }
  }

  # Set index based on level
  if [level] in ["ERROR", "FATAL"] {
    mutate {
      add_tag => ["critical"]
    }
  }

  mutate {
    remove_field => ["json", "message", "ecs", "agent", "host"]
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "java-logs-%{level}-%{+YYYY.MM.dd}"
    ilm_rollover_alias => "java-logs"
    ilm_policy => "java-logs-policy"
  }

  # Debug output
  stdout {
    codec => rubydebug
  }
}
```

### Elasticsearch ILM Policy

```json
// ILM policy — manage log indices lifecycle
PUT _ilm/policy/java-logs-policy
{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
          "rollover": {
            "max_size": "50gb",
            "max_age": "7d"
          },
          "set_priority": {"priority": 100}
        }
      },
      "warm": {
        "min_age": "30d",
        "actions": {
          "forcemerge": {"max_num_segments": 1},
          "shrink": {"number_of_shards": 1},
          "set_priority": {"priority": 50}
        }
      },
      "cold": {
        "min_age": "90d",
        "actions": {
          "set_priority": {"priority": 0},
          "freeze": {}
        }
      },
      "delete": {
        "min_age": "365d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

### Structured Logging with logstash-logback-encoder

```xml
<!-- logback-spring.xml — Full structured JSON logging -->
<configuration>
    <springProperty name="APP_NAME" source="spring.application.name" defaultValue="myapp"/>
    <springProperty name="PROFILE" source="spring.profiles.active" defaultValue="dev"/>

    <!-- Console appender with JSON format for production -->
    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>spanId</includeMdcKeyName>
            <includeMdcKeyName>userId</includeMdcKeyName>
            <includeMdcKeyName>requestId</includeMdcKeyName>
            <fieldNames>
                <timestamp>@timestamp</timestamp>
                <message>message</message>
                <thread>thread</thread>
                <logger>logger</logger>
                <levelValue>[ignore]</levelValue>
            </fieldNames>
            <customFields>{"application":"${APP_NAME}","environment":"${PROFILE}"}</customFields>
        </encoder>
    </appender>

    <!-- TCP appender for Logstash -->
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>${LOGSTASH_HOST:-localhost}:${LOGSTASH_PORT:-5000}</destination>
        <keepAliveDuration>5 minutes</keepAliveDuration>
        <reconnectionDelay>10 seconds</reconnectionDelay>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>spanId</includeMdcKeyName>
        </encoder>
    </appender>

    <!-- Async appender for performance -->
    <appender name="ASYNC_LOGSTASH" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="LOGSTASH"/>
        <queueSize>512</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <neverBlock>true</neverBlock>
    </appender>

    <!-- Audit logger (separate appender) -->
    <appender name="AUDIT" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/audit.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/audit.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>90</maxHistory>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <logger name="AUDIT_LOGGER" level="INFO" additivity="false">
        <appender-ref ref="AUDIT"/>
    </logger>

    <root level="INFO">
        <appender-ref ref="JSON_CONSOLE"/>
        <appender-ref ref="ASYNC_LOGSTASH"/>
    </root>
</configuration>
```

### Structured Logging in Java

```java
// MDC (Mapped Diagnostic Context) — add context to every log line
@WebFilter("/api/*")
public class MdcFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        try {
            MDC.put("requestId", UUID.randomUUID().toString().substring(0, 8));
            MDC.put("userId", request.getParameter("userId"));
            MDC.put("clientIp", request.getRemoteAddr());
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

// Audit logging with structured fields
@Component
public class AuditLogger {
    private static final Logger audit = LoggerFactory.getLogger("AUDIT_LOGGER");

    public void logUserAction(String userId, String action, Map<String, Object> details) {
        // Structured fields automatically included by LogstashEncoder
        audit.info("User action: userId={}, action={}, details={}",
            userId, action, details);
    }
}
```

## Four Golden Signals (Google SRE)

```
LATENCY:    Time taken to service a request
TRAFFIC:    Demand on the system (requests/sec)
ERRORS:     Rate of failed requests (explicit + implicit)
SATURATION: How "full" the service is
```

| Signal | Metric | Alert Threshold |
|--------|--------|-----------------|
| **Latency** | `histogram_quantile(0.99, rate(http_server_requests_seconds_bucket[5m]))` | > 500ms for P99 |
| **Traffic** | `sum(rate(http_server_requests_seconds_count[5m]))` | Sudden drop > 50% (outage) or spike (DDoS) |
| **Errors** | `sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) * 100` | > 1% for 5 min |
| **Saturation** | `jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100` | > 80% CPU, > 85% Memory |

## Exercises

1. Add Micrometer + Prometheus to a Spring Boot app and expose metrics.
2. Create a custom counter and timer metric in a service class.
3. Set up Prometheus to scrape your Spring Boot app.
4. Create a Grafana dashboard with panels for error rate, latency, and JVM heap.
5. Configure structured JSON logging with Logstash and view logs in Kibana.
