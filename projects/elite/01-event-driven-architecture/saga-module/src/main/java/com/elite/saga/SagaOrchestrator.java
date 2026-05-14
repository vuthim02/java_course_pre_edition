package com.elite.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SagaOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(SagaOrchestrator.class);
    private final Map<String, SagaDefinition> sagas = new ConcurrentHashMap<>();

    public void registerSaga(String sagaType, SagaDefinition definition) {
        sagas.put(sagaType, definition);
    }

    public void executeSaga(String sagaType, Object context) {
        SagaDefinition def = sagas.get(sagaType);
        if (def == null) {
            throw new IllegalArgumentException("No saga registered for type: " + sagaType);
        }
        log.info("Executing saga: {}", sagaType);
        def.execute(context);
    }

    public interface SagaDefinition {
        void execute(Object context);
    }
}
