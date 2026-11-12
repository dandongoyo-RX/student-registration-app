package com.example.StudentRegistration.student;

import com.example.StudentRegistration.export.TableExport;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    /** Register a new student. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Student register(@Valid @RequestBody StudentRequest request) {
        return service.register(request);
    }

    /** List every student. */
    @GetMapping
    public List<Student> findAll() {
        return service.findAll();
    }

    /**
     * Download students as a file.
     *   /api/students/export?format=csv            all students as CSV
     *   /api/students/export?format=xlsx           all students as Excel
     *   /api/students/export?format=xlsx&grade=Form%201   one class only
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(@RequestParam(defaultValue = "csv") String format,
                                         @RequestParam(required = false) String grade) {
        List<Student> students = service.findForExport(grade);

        List<String> headers = List.of("ID", "First name", "Middle initial", "Last name", "Gender",
                "Date of birth", "Age", "Nationality", "Class", "Registered", "Last updated");
        DateTimeFormatter timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<List<Object>> rows = new ArrayList<>();
        for (Student s : students) {
            rows.add(Arrays.asList(
                    s.getId(), s.getFirstName(), s.getMiddleInitial(), s.getLastName(), s.getGender(),
                    s.getDateOfBirth().toString(), s.getAge(), s.getNationality(), s.getGrade(),
                    s.getCreatedAt() == null ? null : s.getCreatedAt().format(timestamp),
                    s.getUpdatedAt() == null ? null : s.getUpdatedAt().format(timestamp)));
        }

        String baseName = "students"
                + (grade == null || grade.isBlank() ? "" : "-" + grade.trim().toLowerCase().replace(' ', '-'))
                + "-" + LocalDate.now();

        byte[] file;
        MediaType type;
        String fileName;
        if ("xlsx".equalsIgnoreCase(format) || "excel".equalsIgnoreCase(format)) {
            file = TableExport.toXlsx("Students", headers, rows,
                    new int[]{6, 14, 8, 16, 9, 13, 6, 20, 9, 20, 20});
            type = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            fileName = baseName + ".xlsx";
        } else {
            file = TableExport.toCsv(headers, rows);
            type = MediaType.parseMediaType("text/csv; charset=UTF-8");
            fileName = baseName + ".csv";
        }

        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .body(file);
    }

    /** Get one student by id. */
    @GetMapping("/{id}")
    public Student findById(@PathVariable Long id) {
        return service.findById(id);
    }

    /** Update an existing student. */
    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return service.update(id, request);
    }

    /** Delete a student. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
