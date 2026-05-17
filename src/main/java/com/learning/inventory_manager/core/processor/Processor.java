package com.learning.inventory_manager.core.processor;

public interface Processor<T extends ProcessorRequest, R extends ProcessorResponse> {
    R process(T request);
}
