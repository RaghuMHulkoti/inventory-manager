package com.learning.inventory_manager.common;

public enum StatusCode {
    SUCCESS,
    FAILURE;

    public static boolean isSuccess(StatusCode statusCode) {
        return SUCCESS.equals(statusCode);
    }
}
