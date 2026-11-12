package com.example.StudentRegistration.student;

import com.example.StudentRegistration.common.DuplicateStudentException;
import com.example.StudentRegistration.common.FieldValidationException;
import com.example.StudentRegistration.common.NotFoundException;
import com.example.StudentRegistration.country.Countries;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudentService {

    /** Allowed age range for Form 1 to Form 3 students. Adjust if the school needs different limits. */
    static final int MIN_AGE = 10;
    static final int MAX_AGE = 25;

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Student register(StudentRequest request) {
        checkRules(request);
        checkDuplicate(request, null);
        Student student = new Student(
                clean(request.firstName()),
                clean(request.lastName()),
                initial(request.middleInitial()),
                request.gender(),
                request.dateOfBirth(),
                Countries.canonical(request.nationality()),
                request.grade()
        );
        return repository.save(student);
    }

    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return repository.findAll();
    }

    /** Students for export: all of them, or only one class if grade is given. */
    @Transactional(readOnly = true)
    public List<Student> findForExport(String grade) {
        if (grade == null || grade.isBlank()) {
            return repository.findAllByOrderByIdAsc();
        }
        return repository.findByGradeOrderByIdAsc(grade.trim());
    }

    @Transactional(readOnly = true)
    public Student findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student with id " + id + " was not found"));
    }

    @Transactional
    public Student update(Long id, StudentRequest request) {
        Student student = findById(id);
        checkRules(request);
        checkDuplicate(request, id);
        student.setFirstName(clean(request.firstName()));
        student.setLastName(clean(request.lastName()));
        student.setMiddleInitial(initial(request.middleInitial()));
        student.setGender(request.gender());
        student.setDateOfBirth(request.dateOfBirth());
        student.setNationality(Countries.canonical(request.nationality()));
        student.setGrade(request.grade());
        // saveAndFlush writes the change now, so updatedAt is refreshed in the response
        return repository.saveAndFlush(student);
    }

    @Transactional
    public void delete(Long id) {
        Student student = findById(id);
        repository.delete(student);
    }

    /** Rules that need more than a simple annotation. */
    private void checkRules(StudentRequest request) {
        int age = Student.ageOn(request.dateOfBirth(), LocalDate.now());
        if (age < MIN_AGE || age > MAX_AGE) {
            throw new FieldValidationException("dateOfBirth",
                    "Student must be between " + MIN_AGE + " and " + MAX_AGE + " years old (this date gives " + age + ")");
        }
        if (Countries.canonical(request.nationality()) == null) {
            throw new FieldValidationException("nationality", "Please choose a country from the list");
        }
    }

    /**
     * Stops the same student being registered twice: same first name, last name
     * (ignoring capitals) and date of birth. When updating, the student's own record is ignored.
     */
    private void checkDuplicate(StudentRequest request, Long ownId) {
        repository.findFirstByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndDateOfBirth(
                        clean(request.firstName()), clean(request.lastName()), request.dateOfBirth())
                .filter(existing -> !existing.getId().equals(ownId))
                .ifPresent(existing -> {
                    throw new DuplicateStudentException(
                            "This student is already registered (ID " + existing.getId() + ", "
                                    + existing.getFirstName() + " " + existing.getLastName() + ", "
                                    + existing.getGrade() + ")");
                });
    }

    private static String clean(String value) {
        return value == null ? null : value.trim();
    }

    /** Stores the middle initial as one capital letter, or null if none was given. */
    private static String initial(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }
}
