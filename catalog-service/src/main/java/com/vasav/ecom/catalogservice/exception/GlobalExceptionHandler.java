package com.vasav.ecom.catalogservice.exception;

import com.vasav.ecom.catalogservice.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Spring MVC exceptions (ResponseStatusException, bad JSON, missing params/headers, type mismatch,
 * unknown route, ...) are handled by {@link ResponseEntityExceptionHandler} and rendered through
 * {@link #handleExceptionInternal} so every error keeps the {@link ErrorResponse} shape.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
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
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
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

    @ExceptionHandler({ProductNotFoundException.class, ReviewNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({NotReviewAuthorException.class, NotVerifiedPurchaseException.class})
    public ResponseEntity<ErrorResponse> handleForbidden(RuntimeException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)   // @PreAuthorize failures (AuthorizationDeniedException)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Access denied");
    }

    @ExceptionHandler(AlreadyReviewedException.class)
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(VersionMismatchException.class)
    public ResponseEntity<ErrorResponse> handleVersionMismatch(VersionMismatchException ex) {
        return build(HttpStatus.PRECONDITION_FAILED, ex.getMessage());
    }

    // two writers passed the version check at the same time; the loser's flush fails on @Version
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex) {
        return build(HttpStatus.PRECONDITION_FAILED, "Resource was modified concurrently, reload it");
    }

    @ExceptionHandler(PreconditionRequiredException.class)
    public ResponseEntity<ErrorResponse> handlePreconditionRequired(PreconditionRequiredException ex) {
        return build(HttpStatus.PRECONDITION_REQUIRED, ex.getMessage());
    }

    @ExceptionHandler({InvalidPreconditionException.class, PropertyReferenceException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
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
