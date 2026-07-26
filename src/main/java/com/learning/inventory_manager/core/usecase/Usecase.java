package com.learning.inventory_manager.core.usecase;

public interface Usecase<T extends UsecaseRequest, R extends UsecaseResponse> {
    R execute(T request);
}
