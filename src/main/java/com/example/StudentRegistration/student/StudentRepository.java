package com.example.StudentRegistration.student;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring generates save(), findAll(), findById(), deleteById() and more for us.
 * It also writes the SQL for the extra methods below from their names.
 */
public interface StudentRepository extends JpaRepository<Student, Long> {

    /** Used by the duplicate check: same first name, last name (any capitalisation) and date of birth. */
    Optional<Student> findFirstByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDateOfBirth(
            String firstName, String lastName, LocalDate dateOfBirth);

    /** All students, ordered by ID (for export). */
    List<Student> findAllByOrderByIdAsc();

    /** Students in one class, ordered by ID (for export). */
    List<Student> findByGradeOrderByIdAsc(String grade);
}
