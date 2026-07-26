package com.learning.inventory_manager.core.service;

public interface Service<T extends ServiceRequest, R extends ServiceResponse> {
    R execute(T request);
}
