package com.example.StudentRegistration.common;

/** Raised when one field breaks a business rule (e.g. the student is too young). */
public class FieldValidationException extends RuntimeException {

    private final String field;

    public FieldValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
