package com.elite.es.store;

import com.elite.es.event.OrderCreatedEvent;
import com.elite.es.event.OrderShippedEvent;
import java.util.List;

public interface EventStore {
    void append(String aggregateId, Object event, long version);
    List<Object> readEvents(String aggregateId);
    long getCurrentVersion(String aggregateId);
}
