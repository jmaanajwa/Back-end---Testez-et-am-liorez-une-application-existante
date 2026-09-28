package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import java.util.List;
import java.util.Optional;

/**
 * Manages transactional student lookup and modification operations.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;

    /**
     * Creates a student through the JPA repository, ignoring any supplied ID.
     *
     * @param student the student data to save; its ID is cleared before saving
     * @return the saved student
     * @throws IllegalArgumentException if the student is null
     */
    public Student create(Student student) {
        Assert.notNull(student, "Student request must not be null");
        student.setId(null);
        return studentRepository.save(student);
    }

    /**
     * Retrieves all registered students.
     *
     * @return the possibly empty list of students
     */
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    /**
     * Finds a student by ID.
     *
     * @param id the nonnull student ID
     * @return the matching student, or an empty result if absent
     * @throws IllegalArgumentException if the ID is null
     */
    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    /**
     * Deletes a student if present.
     *
     * @param id the nonnull student ID
     * @return {@code true} if the student was deleted, or {@code false} if absent
     * @throws IllegalArgumentException if the ID is null
     */
    public boolean delete(Long id) {
        Optional<Student> student = studentRepository.findById(id);
        if (student.isEmpty()) {
            return false;
        }
        studentRepository.deleteById(student.get().getId());
        return true;
    }

    /**
     * Replaces the last name, first name, email address, and class of an existing student.
     * The ID carried by the new data is ignored.
     *
     * @param id the nonnull ID of the student to update
     * @param student the new details to apply
     * @return the updated student, or an empty result if no student matches the ID
     * @throws IllegalArgumentException if the ID or new student data is null
     */
    public Optional<Student> update(Long id, Student student) {
        Assert.notNull(student, "Student request must not be null");
        return studentRepository.findById(id).map(existingStudent -> {
            existingStudent.setLastName(student.getLastName());
            existingStudent.setFirstName(student.getFirstName());
            existingStudent.setEmail(student.getEmail());
            existingStudent.setClassName(student.getClassName());
            return studentRepository.save(existingStudent);
        });
    }
}
