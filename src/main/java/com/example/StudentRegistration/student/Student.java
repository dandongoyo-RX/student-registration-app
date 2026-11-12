package com.example.StudentRegistration.student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * One row in the "students" table.
 */
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    /** A single letter, e.g. "K". Optional because not everyone has a middle name. */
    @Column(length = 1)
    private String middleInitial;

    /** "Male" or "Female". Required by the API; nullable in the table so older rows still load. */
    @Column(length = 10)
    private String gender;

    /** Stored instead of age, because age changes every year but a birth date never does. */
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    /** Country name, chosen from the list at /api/countries. */
    @Column(nullable = false, length = 100)
    private String nationality;

    /** "Form 1", "Form 2" or "Form 3". */
    @Column(nullable = false, length = 10)
    private String grade;

    /** Filled in automatically when the record is first saved. Never changes. */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Filled in automatically every time the record is saved. */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Student() {
        // required by JPA
    }

    public Student(String firstName, String lastName, String middleInitial, String gender,
                   LocalDate dateOfBirth, String nationality, String grade) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleInitial = middleInitial;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
        this.grade = grade;
    }

    /** Age in whole years, worked out from the date of birth (not stored in the database). */
    public int getAge() {
        return ageOn(dateOfBirth, LocalDate.now());
    }

    static int ageOn(LocalDate dateOfBirth, LocalDate today) {
        return Period.between(dateOfBirth, today).getYears();
    }

    public Long getId() { return id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleInitial() { return middleInitial; }
    public void setMiddleInitial(String middleInitial) { this.middleInitial = middleInitial; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
