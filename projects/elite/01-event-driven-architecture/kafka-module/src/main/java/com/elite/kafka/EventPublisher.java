package com.elite.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class EventPublisher implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);
    private final KafkaProducer<String, String> producer;

    public EventPublisher(KafkaConfig config) {
        this.producer = new KafkaProducer<>(config.producerProperties());
    }

    public Future<RecordMetadata> publish(String topic, String key, String value) {
        log.debug("Publishing event to {}: key={}", topic, key);
        return producer.send(new ProducerRecord<>(topic, key, value),
            (metadata, exception) -> {
                if (exception != null) {
                    log.error("Failed to publish event to topic {}: {}", topic, exception.getMessage(), exception);
                } else {
                    log.debug("Published event to {} partition={} offset={}", topic, metadata.partition(), metadata.offset());
                }
            });
    }

    @Override
    public void close() {
        producer.close();
    }
}
