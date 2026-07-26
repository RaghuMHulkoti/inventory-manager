package com.learning.inventory_manager.web.response;

import com.learning.inventory_manager.common.StatusCode;

public class Response<T> {
    private StatusCode status;
    private String message;
    private T body;

    public Response() {}

    public Response(StatusCode status, String message) {
        this.status = status;
        this.message = message;
    }

    public Response(StatusCode status, String message, T body) {
        this.status = status;
        this.message = message;
        this.body = body;
    }

    public Response(T body) {
        this.status = StatusCode.SUCCESS;
        this.body = body;
    }

    public StatusCode getStatus() {
        return status;
    }

    public void setStatus(StatusCode status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
