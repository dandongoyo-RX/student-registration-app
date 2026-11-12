package com.example.StudentRegistration.common;

/** Raised when a student with the same name and date of birth is already registered. */
public class DuplicateStudentException extends RuntimeException {

    public DuplicateStudentException(String message) {
        super(message);
    }
}
