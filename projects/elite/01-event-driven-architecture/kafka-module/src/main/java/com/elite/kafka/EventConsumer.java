package com.elite.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.function.BiConsumer;

public class EventConsumer implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);
    private final KafkaConsumer<String, String> consumer;
    private volatile boolean running = true;

    public EventConsumer(KafkaConfig config, String groupId) {
        this.consumer = new KafkaConsumer<>(config.consumerProperties(groupId));
    }

    public void subscribe(String topic, BiConsumer<String, String> handler) {
        consumer.subscribe(Collections.singletonList(topic));
        log.info("Subscribed to topic: {}", topic);
        Thread consumerThread = new Thread(() -> {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        handler.accept(record.key(), record.value());
                    } catch (Exception e) {
                        log.error("Error processing record from topic {}: {}", topic, e.getMessage(), e);
                    }
                }
                consumer.commitSync();
            }
        }, "kafka-consumer-" + topic);
        consumerThread.setDaemon(true);
        consumerThread.start();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void close() {
        stop();
        consumer.close();
    }
}
