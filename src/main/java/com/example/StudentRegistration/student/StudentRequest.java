package com.example.StudentRegistration.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * The data a client sends to register or update a student.
 * id, age, createdAt and updatedAt are NOT here: the system works those out itself.
 */
public record StudentRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        @Pattern(regexp = "^[A-Za-z]?$", message = "Middle initial must be a single letter")
        String middleInitial,

        @NotBlank(message = "Gender is required")
        @Pattern(regexp = "^(Male|Female)$", message = "Gender must be Male or Female")
        String gender,

        /* Sent as "YYYY-MM-DD", e.g. "2011-03-24". */
        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotBlank(message = "Nationality is required")
        String nationality,

        @NotBlank(message = "Class is required")
        @Pattern(regexp = "^Form [123]$", message = "Class must be Form 1, Form 2 or Form 3")
        String grade
) {
}
