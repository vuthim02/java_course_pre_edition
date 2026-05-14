package com.elite.es.store;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class InMemoryEventStore implements EventStore {
    private final Map<String, List<Object>> store = new ConcurrentHashMap<>();
    private final Map<String, Long> versions = new ConcurrentHashMap<>();

    @Override
    public void append(String aggregateId, Object event, long version) {
        store.computeIfAbsent(aggregateId, k -> new CopyOnWriteArrayList<>()).add(event);
        versions.put(aggregateId, version);
    }

    @Override
    public List<Object> readEvents(String aggregateId) {
        return store.getOrDefault(aggregateId, List.of());
    }

    @Override
    public long getCurrentVersion(String aggregateId) {
        return versions.getOrDefault(aggregateId, 0L);
    }
}
