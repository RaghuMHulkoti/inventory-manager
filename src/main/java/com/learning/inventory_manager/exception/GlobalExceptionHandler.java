package com.learning.inventory_manager.exception;

import com.learning.inventory_manager.common.StatusCode;
import com.learning.inventory_manager.web.response.ErrorResponse;
import com.learning.inventory_manager.web.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ResponseEntity<Response<ErrorResponse>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        var errorResponse = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Request validation failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .details(errors)
                .build();

        return new ResponseEntity<>(new Response<>(StatusCode.FAILURE, "Request validation failed", errorResponse), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle warehouse not found errors.
     */
    @ExceptionHandler(WarehouseNotFoundException.class)
    public ResponseEntity<Response<ErrorResponse>> handleWarehouseNotFound(
            WarehouseNotFoundException ex
    ) {
        log.warn("Warehouse not found: {}", ex.getMessage());

        var errorResponse = ErrorResponse.builder()
                .errorCode("WAREHOUSE_NOT_FOUND")
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(new Response<>(StatusCode.FAILURE, ex.getMessage(), errorResponse), HttpStatus.NOT_FOUND);
    }

    /**
     * Handle optimistic locking failures.
     */
    @ExceptionHandler(OptimisticLockingException.class)
    public ResponseEntity<Response<ErrorResponse>> handleOptimisticLocking(
            OptimisticLockingException ex
    ) {
        log.warn("Optimistic locking failure: {}", ex.getMessage());

        var errorResponse = ErrorResponse.builder()
                .errorCode("OPTIMISTIC_LOCKING_FAILURE")
                .message("The resource has been modified by another process. Please retry.")
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(new Response<>(StatusCode.FAILURE, "Optimistic locking failure", errorResponse), HttpStatus.CONFLICT);
    }

    /**
     * Handle insufficient stock during reservation.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Response<ErrorResponse>> handleInsufficientStock(
            InsufficientStockException ex
    ) {
        log.warn("Insufficient stock: {}", ex.getMessage());

        Map<String, String> details = new HashMap<>();
        details.put("supplierProductId", String.valueOf(ex.getSupplierProductId()));
        details.put("warehouseId", String.valueOf(ex.getWarehouseId()));
        details.put("available", String.valueOf(ex.getAvailable()));
        details.put("requested", String.valueOf(ex.getRequested()));

        var errorResponse = ErrorResponse.builder()
                .errorCode("INSUFFICIENT_STOCK")
                .message("Insufficient stock to fulfill reservation")
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();

        return new ResponseEntity<>(new Response<>(StatusCode.FAILURE, "Insufficient stock", errorResponse), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Handle reservation not found errors.
     */
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<Response<ErrorResponse>> handleReservationNotFound(
            ReservationNotFoundException ex
    ) {
        log.warn("Reservation not found: {}", ex.getMessage());

        var errorResponse = ErrorResponse.builder()
                .errorCode("RESERVATION_NOT_FOUND")
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(new Response<>(StatusCode.FAILURE, ex.getMessage(), errorResponse), HttpStatus.NOT_FOUND);
    }

    /**
     * Handle other inventory exceptions.
     */
    @ExceptionHandler(InventoryException.class)
    public ResponseEntity<Response<ErrorResponse>> handleInventoryException(
            InventoryException ex
    ) {
        log.error("Inventory operation failed: {}", ex.getMessage(), ex);

        var errorResponse = ErrorResponse.builder()
                .errorCode("INVENTORY_OPERATION_FAILED")
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(new Response<>(StatusCode.FAILURE, ex.getMessage(), errorResponse), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<ErrorResponse>> handleUnexpectedException(Exception ex) {
        log.error("Unexpected error", ex);

        var errorResponse = ErrorResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .message("An unexpected error occurred. Please contact support.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(new Response<>(StatusCode.FAILURE, "Unexpected error", errorResponse), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
