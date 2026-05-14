package com.elite.saga;

public interface SagaStep<T> {
    void execute(T context);
    void compensate(T context);
}
