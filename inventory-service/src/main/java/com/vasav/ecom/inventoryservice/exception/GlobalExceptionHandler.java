package com.vasav.ecom.inventoryservice.exception;

import com.vasav.ecom.inventoryservice.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request
    ) {

        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));

        return ResponseEntity.status(status).headers(headers)
                .body(new ErrorResponse(status.value(), "Validation failed", Instant.now(), errors));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request
    ) {
        // body is null for ErrorResponse exceptions (e.g. ResponseStatusException): their detail is on the exception
        ProblemDetail problem = body instanceof ProblemDetail p ? p
                : ex instanceof org.springframework.web.ErrorResponse errorResponse ? errorResponse.getBody()
                : null;
        String message = problem != null && problem.getDetail() != null
                ? problem.getDetail()
                : HttpStatus.valueOf(statusCode.value()).getReasonPhrase();

        if (statusCode.is5xxServerError()) {
            log.error("Request failed", ex);
        }

        return ResponseEntity.status(statusCode).headers(headers)
                .body(new ErrorResponse(statusCode.value(), message, Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message, Instant.now()));
    }
}
