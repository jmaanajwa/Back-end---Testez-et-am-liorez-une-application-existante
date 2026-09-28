package com.openclassrooms.etudiant.controller;

import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.service.StudentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import java.util.List;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes student management operations under {@code /api/students}.
 */
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {
    private final StudentService studentService;

    /**
     * Lists all registered students.
     *
     * @return an HTTP 200 response containing the possibly empty student list
     */
    @GetMapping
    public ResponseEntity<List<Student>> findAll() {
        return ResponseEntity.ok(studentService.findAll());
    }

    /**
     * Saves student data received as JSON.
     *
     * @param student the student to save
     * @return an HTTP 201 response containing the saved student
     */
    @PostMapping
    public ResponseEntity<Student> create(@Valid @RequestBody Student student) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(student));
    }

    /**
     * Finds a student by ID.
     *
     * @param id the student ID
     * @return an HTTP 200 response with the student, or HTTP 404 if absent
     */
    @GetMapping("/{id}")
    public ResponseEntity<Student> findById(@PathVariable @NotNull  Long id) {
        return ResponseEntity.of(studentService.findById(id));
    }

    /**
     * Replaces the details of an existing student.
     *
     * @param id the ID of the student to update
     * @param student the new student details
     * @return an HTTP 200 response with the updated student, or HTTP 404 if absent
     */
    @PutMapping("/{id}")
    public ResponseEntity<Student> update(@PathVariable @NotNull Long id, @Valid @RequestBody Student student) {
        return ResponseEntity.of(studentService.update(id, student));
    }

    /**
     * Deletes a student by ID.
     *
     * @param id the ID of the student to delete
     * @return an HTTP 204 response without content, or HTTP 404 if the student is absent
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @NotNull Long id) {
        if (!studentService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
