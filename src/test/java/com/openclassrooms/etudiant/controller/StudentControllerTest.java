package com.openclassrooms.etudiant.controller;

import com.openclassrooms.etudiant.repository.StudentRepository;
import com.openclassrooms.etudiant.service.StudentService;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.handler.RestExceptionHandler;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StudentControllerTest {
    private StudentRepository repository;
    private MockMvc mockMvc;

    @Test
    void deletesExistingStudent() throws Exception {
        Student student = new Student(7L, "Martin", "Lea", "lea@example.com", "L1");
        when(repository.findById(7L)).thenReturn(Optional.of(student));

        mockMvc.perform(delete("/api/students/7"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(repository).findById(7L);
        verify(repository).deleteById(7L);
    }

    @Test
    void deleteReturnsNotFoundForMissingStudent() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/students/99"))
                .andExpect(status().isNotFound());
        verify(repository).findById(99L);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void updatesStudentAndPreservesId() throws Exception {
        Student student = new Student(7L, "Martin", "Lea", "lea@example.com", "L1");
        when(repository.findById(7L)).thenReturn(Optional.of(student));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/api/students/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":99,"lastName":"Dupont","firstName":"Najwa",
                                 "email":"najwa@example.com","className":"L2"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.firstName").value("Najwa"))
                .andExpect(jsonPath("$.email").value("najwa@example.com"))
                .andExpect(jsonPath("$.className").value("L2"));
        verify(repository).save(same(student));
    }

    @Test
    void updateReturnsNotFoundForMissingStudent() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/students/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Dupont","firstName":"Najwa",
                                 "email":"najwa@example.com","className":"L2"}
                                """))
                .andExpect(status().isNotFound());
        verify(repository).findById(99L);
        verify(repository, never()).save(any());
    }

    @Test
    void getsStudentById() throws Exception {
        Student student = new Student();
        student.setId(7L);
        student.setLastName("Dupont");
        student.setFirstName("Najwa");
        student.setEmail("najwa@example.com");
        student.setClassName("L1");
        when(repository.findById(7L)).thenReturn(Optional.of(student));

        mockMvc.perform(get("/api/students/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.firstName").value("Najwa"))
                .andExpect(jsonPath("$.email").value("najwa@example.com"))
                .andExpect(jsonPath("$.className").value("L1"));
        verify(repository).findById(7L);
    }

    @Test
    void returnsNotFoundForMissingStudent() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound());
        verify(repository).findById(99L);
    }

    @Test
    void listsStudents() throws Exception {
        Student student = new Student();
        student.setId(1L);
        student.setLastName("Dupont");
        student.setFirstName("Najwa");
        student.setEmail("najwa@example.com");
        student.setClassName("L1");
        when(repository.findAll()).thenReturn(List.of(student));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].lastName").value("Dupont"))
                .andExpect(jsonPath("$[0].firstName").value("Najwa"))
                .andExpect(jsonPath("$[0].email").value("najwa@example.com"))
                .andExpect(jsonPath("$[0].className").value("L1"));
        verify(repository).findAll();
    }

    @Test
    void listsEmptyStudents() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        verify(repository).findAll();
    }

    @BeforeEach
    void setUp() {
        repository = mock(StudentRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new StudentController(new StudentService(repository)))
                .setControllerAdvice(new RestExceptionHandler()).build();
    }

    @Test
    void rejectsEmptyBodiesWithProblemDetail() throws Exception {
        for (String body : List.of("", "null")) {
            mockMvc.perform(post("/api/students")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("Failed to read request"));
            mockMvc.perform(put("/api/students/7")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.detail").value("Failed to read request"));
        }
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsPutAndDeleteOnCollectionRoute() throws Exception {
        mockMvc.perform(put("/api/students").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Dupont","firstName":"Najwa",
                                 "email":"najwa@example.com","className":"L1"}
                                """))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
        mockMvc.perform(delete("/api/students"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
        verifyNoInteractions(repository);
    }

    @Test
    void returnsNotFoundForPutAndDeleteOnUnmappedTrailingSlashRoute() throws Exception {
        mockMvc.perform(put("/api/students/").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Dupont","firstName":"Najwa",
                                 "email":"najwa@example.com","className":"L1"}
                                """))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/students/"))
                .andExpect(status().isNotFound());
        verifyNoInteractions(repository);
    }

    @Test
    void createsStudentWithGeneratedId() throws Exception {
        when(repository.save(any())).thenAnswer(invocation -> {
            com.openclassrooms.etudiant.entities.Student student = invocation.getArgument(0);
            assertThat(student.getId()).isNull();
            student.setId(1L);
            return student;
        });

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":99,"lastName":"Dupont","firstName":"Najwa",
                                 "email":"najwa@example.com","className":"L1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.firstName").value("Najwa"))
                .andExpect(jsonPath("$.email").value("najwa@example.com"))
                .andExpect(jsonPath("$.className").value("L1"));
        verify(repository).save(any());
    }

    @Test
    void rejectsMissingNames() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Dupont","firstName":"Najwa","email":"invalid","className":"L1"}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsBlankNames() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":" ","firstName":"","email":"najwa@example.com","className":"L1"}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsInvalidEmailOnUpdate() throws Exception {
        mockMvc.perform(put("/api/students/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastName":"Dupont","firstName":"Najwa","email":"invalid","className":"L1"}
                                """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }
}
