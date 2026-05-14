# Zipkin Distributed Tracing Setup

## Running Zipkin

```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

## Configuration

Each service uses Micrometer Tracing with Brave to send traces to Zipkin.

### Dependencies (already in pom.xml):
- `micrometer-tracing-bridge-brave`
- `zipkin-reporter-brave`

### application.yml (already configured in gateway):
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

### Tracing in services

For each service, add these dependencies:
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

### Viewing Traces

1. Start Zipkin: `docker run -d -p 9411:9411 openzipkin/zipkin`
2. Navigate to http://localhost:9411
3. Search by service name, duration, tags, etc.
4. Click on a trace to see the full span tree across services
