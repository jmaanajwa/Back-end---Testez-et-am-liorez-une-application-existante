package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.repository.StudentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class StudentServiceTest {
    private static final Long STUDENT_ID = 7L;
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final String CLASS_NAME = "L1";

    @Mock
    private StudentRepository studentRepository;
    @InjectMocks
    private StudentService studentService;

    @Test
    public void test_create_null_student_throws_IllegalArgumentException() {
        // GIVEN

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> studentService.create(null));
        verifyNoInteractions(studentRepository);
    }

    @Test
    public void test_create_student() {
        // GIVEN
        Student student = new Student(null, LAST_NAME, FIRST_NAME, EMAIL, CLASS_NAME);
        Student savedStudent = new Student(STUDENT_ID, LAST_NAME, FIRST_NAME, EMAIL, CLASS_NAME);
        when(studentRepository.save(student)).thenReturn(savedStudent);

        // WHEN
        Student result = studentService.create(student);

        // THEN
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentCaptor.capture());
        assertThat(studentCaptor.getValue()).isSameAs(student);
        assertThat(studentCaptor.getValue().getId()).isNull();
        assertThat(result).isSameAs(savedStudent);
        assertThat(result.getId()).isEqualTo(STUDENT_ID);
    }

    @Test
    public void test_find_all_students() {
        // GIVEN
        Student firstStudent = new Student(STUDENT_ID, LAST_NAME, FIRST_NAME, EMAIL, CLASS_NAME);
        Student secondStudent = new Student(8L, "Martin", "Lea", "lea@example.com", "L2");
        when(studentRepository.findAll()).thenReturn(List.of(firstStudent, secondStudent));

        // WHEN
        List<Student> result = studentService.findAll();

        // THEN
        assertThat(result).containsExactly(firstStudent, secondStudent);
        verify(studentRepository).findAll();
    }

    @Test
    public void test_find_all_students_returns_empty_list() {
        // GIVEN
        when(studentRepository.findAll()).thenReturn(List.of());

        // WHEN
        List<Student> result = studentService.findAll();

        // THEN
        assertThat(result).isEmpty();
        verify(studentRepository).findAll();
    }

    @Test
    public void test_find_student_by_id() {
        // GIVEN
        Student student = new Student(STUDENT_ID, LAST_NAME, FIRST_NAME, EMAIL, CLASS_NAME);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        // WHEN
        Optional<Student> result = studentService.findById(STUDENT_ID);

        // THEN
        assertThat(result).containsSame(student);
        verify(studentRepository).findById(STUDENT_ID);
    }

    @Test
    public void test_find_missing_student_by_id_returns_empty() {
        // GIVEN
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        // WHEN
        Optional<Student> result = studentService.findById(STUDENT_ID);

        // THEN
        assertThat(result).isEmpty();
        verify(studentRepository).findById(STUDENT_ID);
    }

    @Test
    public void test_delete_student() {
        // GIVEN
        Student student = new Student(STUDENT_ID, LAST_NAME, FIRST_NAME, EMAIL, CLASS_NAME);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(student));

        // WHEN
        boolean result = studentService.delete(STUDENT_ID);

        // THEN
        assertThat(result).isTrue();
        verify(studentRepository).deleteById(STUDENT_ID);
        verify(studentRepository).findById(STUDENT_ID);
    }

    @Test
    public void test_delete_missing_student_returns_false() {
        // GIVEN
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        // WHEN
        boolean result = studentService.delete(STUDENT_ID);

        // THEN
        assertThat(result).isFalse();
        verify(studentRepository).findById(STUDENT_ID);
        verify(studentRepository, never()).deleteById(any());
    }

    @Test
    public void test_update_null_student_throws_IllegalArgumentException() {
        // GIVEN

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> studentService.update(STUDENT_ID, null));
        verifyNoInteractions(studentRepository);
    }

    @Test
    public void test_update_student_preserves_id() {
        // GIVEN
        Student existingStudent = new Student(STUDENT_ID, LAST_NAME, FIRST_NAME, EMAIL, CLASS_NAME);
        Student updatedStudent = new Student(99L, "Martin", "Lea", "lea@example.com", "L2");
        Student savedStudent = new Student(STUDENT_ID, "Martin", "Lea", "lea@example.com", "L2");
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.of(existingStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        // WHEN
        Optional<Student> result = studentService.update(STUDENT_ID, updatedStudent);

        // THEN
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(studentCaptor.capture());
        Student capturedStudent = studentCaptor.getValue();
        assertThat(capturedStudent).isSameAs(existingStudent);
        assertThat(capturedStudent.getId()).isEqualTo(STUDENT_ID);
        assertThat(capturedStudent.getLastName()).isEqualTo(updatedStudent.getLastName());
        assertThat(capturedStudent.getFirstName()).isEqualTo(updatedStudent.getFirstName());
        assertThat(capturedStudent.getEmail()).isEqualTo(updatedStudent.getEmail());
        assertThat(capturedStudent.getClassName()).isEqualTo(updatedStudent.getClassName());
        assertThat(result).containsSame(savedStudent);
        verify(studentRepository).findById(STUDENT_ID);
    }

    @Test
    public void test_update_missing_student_returns_empty() {
        // GIVEN
        Student student = new Student(null, LAST_NAME, FIRST_NAME, EMAIL, CLASS_NAME);
        when(studentRepository.findById(STUDENT_ID)).thenReturn(Optional.empty());

        // WHEN
        Optional<Student> result = studentService.update(STUDENT_ID, student);

        // THEN
        assertThat(result).isEmpty();
        verify(studentRepository).findById(STUDENT_ID);
        verify(studentRepository, never()).save(any(Student.class));
    }
}
