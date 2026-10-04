package com.learning.inventory_manager.exception;

import com.learning.inventory_manager.common.StatusCode;
import com.learning.inventory_manager.web.response.ErrorResponse;
import com.learning.inventory_manager.web.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST controllers.
 * Converts domain exceptions to appropriate HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors (request format).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<ErrorResponse>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        // Class-level constraints produce ObjectErrors, not FieldErrors — key them by object name
        ex.getBindingResult().getAllErrors().forEach(error -> errors.put(
                error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName(),
                error.getDefaultMessage()));

        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", errors);
    }

    /**
     * Handle malformed requests: unreadable JSON, missing or wrongly typed parameters/headers.
     */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Response<ErrorResponse>> handleBadRequest(Exception ex) {
        log.warn("Malformed request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Malformed request: check body, parameters and headers", null);
    }

    @ExceptionHandler(InventoryValidationException.class)
    public ResponseEntity<Response<ErrorResponse>> handleInventoryValidation(InventoryValidationException ex) {
        log.warn("Inventory validation failed: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), null);
    }

    @ExceptionHandler(WarehouseNotFoundException.class)
    public ResponseEntity<Response<ErrorResponse>> handleWarehouseNotFound(WarehouseNotFoundException ex) {
        log.warn("Warehouse not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "WAREHOUSE_NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(InventoryNotFoundException.class)
    public ResponseEntity<Response<ErrorResponse>> handleInventoryNotFound(InventoryNotFoundException ex) {
        log.warn("Inventory not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "INVENTORY_NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<Response<ErrorResponse>> handleReservationNotFound(ReservationNotFoundException ex) {
        log.warn("Reservation not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(OptimisticLockingException.class)
    public ResponseEntity<Response<ErrorResponse>> handleOptimisticLocking(OptimisticLockingException ex) {
        log.warn("Optimistic locking failure: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "OPTIMISTIC_LOCKING_FAILURE",
                "The resource has been modified by another process. Please retry.", null);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Response<ErrorResponse>> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("Insufficient stock: {}", ex.getMessage());

        Map<String, String> details = new HashMap<>();
        details.put("supplierProductId", String.valueOf(ex.getSupplierProductId()));
        details.put("warehouseId", String.valueOf(ex.getWarehouseId()));
        details.put("available", String.valueOf(ex.getAvailable()));
        details.put("requested", String.valueOf(ex.getRequested()));

        return build(HttpStatus.UNPROCESSABLE_CONTENT, "INSUFFICIENT_STOCK",
                "Insufficient stock to fulfill reservation", details);
    }

    /**
     * Handle other inventory exceptions.
     */
    @ExceptionHandler(InventoryException.class)
    public ResponseEntity<Response<ErrorResponse>> handleInventoryException(InventoryException ex) {
        log.error("Inventory operation failed: {}", ex.getMessage(), ex);
        return build(HttpStatus.BAD_REQUEST, "INVENTORY_OPERATION_FAILED", ex.getMessage(), null);
    }

    /**
     * Handle unexpected exceptions. Spring MVC exceptions that already carry an HTTP status
     * (404 no route, 405 wrong method, 415 wrong content type, ...) keep that status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<ErrorResponse>> handleUnexpectedException(Exception ex) {
        if (ex instanceof org.springframework.web.ErrorResponse springError) {
            HttpStatusCode status = springError.getStatusCode();
            log.warn("Request rejected with {}: {}", status, ex.getMessage());
            return build(status, "REQUEST_REJECTED", ex.getMessage(), null);
        }

        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred. Please contact support.", null);
    }

    private ResponseEntity<Response<ErrorResponse>> build(HttpStatusCode status, String errorCode,
                                                         String message, Map<String, String> details) {
        var errorResponse = ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();

        return ResponseEntity.status(status).body(new Response<>(StatusCode.FAILURE, message, errorResponse));
    }
}
