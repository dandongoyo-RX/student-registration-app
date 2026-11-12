package com.example.StudentRegistration.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns errors into clear JSON responses instead of a generic error page.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404 when a student id doesn't exist. */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 404);
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /** 400 with a message per field when the submitted data is invalid. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return validationResponse(fieldErrors);
    }

    /** 400 when a business rule fails for one field (e.g. age out of range). */
    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<Map<String, Object>> handleFieldRule(FieldValidationException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        fieldErrors.put(ex.getField(), ex.getMessage());
        return validationResponse(fieldErrors);
    }

    /** 409 when the same student is already registered. */
    @ExceptionHandler(DuplicateStudentException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateStudentException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        fieldErrors.put("firstName", ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 409);
        body.put("error", ex.getMessage());
        body.put("fields", fieldErrors);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /** 400 when the JSON can't be read, e.g. a date that isn't in YYYY-MM-DD format. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Invalid request data. Dates must be in YYYY-MM-DD format.");
        return ResponseEntity.badRequest().body(body);
    }

    private static ResponseEntity<Map<String, Object>> validationResponse(Map<String, String> fieldErrors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Validation failed");
        body.put("fields", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }
}
