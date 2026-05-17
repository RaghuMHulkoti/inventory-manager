package com.learning.inventory_manager.web.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response format for REST APIs.
 */
public record ErrorResponse(
    String errorCode,
    String message,
    int status,
    LocalDateTime timestamp,
    Map<String, String> details
) {
}

