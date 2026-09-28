package com.openclassrooms.etudiant.entities;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;

class StudentTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private Student validStudent() {
        return new Student(null, "Dupont", "Lea", "lea@example.com", "L1");
    }

    @Test
    void acceptsValidStudentBeforeIdIsGenerated() {
        Student student = validStudent();
        assertThat(student.getId()).isNull();
        assertThat(validator.validate(student)).isEmpty();
    }

    @Test
    void constructorPreservesStudentDetails() {
        Student student = new Student(7L, "Dupont", "Lea", "lea@example.com", "L1");
        assertThat(student.getId()).isEqualTo(7L);
        assertThat(student.getLastName()).isEqualTo("Dupont");
        assertThat(student.getFirstName()).isEqualTo("Lea");
        assertThat(student.getEmail()).isEqualTo("lea@example.com");
        assertThat(student.getClassName()).isEqualTo("L1");
    }

    static Stream<Arguments> blankFields() {
        return Stream.of("lastName", "firstName", "email", "className")
                .flatMap(field -> Stream.of(null, "", "   ").map(value -> Arguments.of(field, value)));
    }

    @ParameterizedTest
    @MethodSource("blankFields")
    void rejectsEachMissingRequiredField(String field, String value) {
        Student student = validStudent();
        switch (field) {
            case "lastName" -> student.setLastName(value);
            case "firstName" -> student.setFirstName(value);
            case "email" -> student.setEmail(value);
            case "className" -> student.setClassName(value);
            default -> throw new IllegalArgumentException(field);
        }
        var violations = validator.validate(student);
        assertThat(violations).isNotEmpty();
        assertThat(violations).allSatisfy(violation ->
                assertThat(violation.getPropertyPath().toString()).isEqualTo(field));
        assertThat(violations).anySatisfy(violation ->
                assertThat(violation.getMessage()).isEqualTo(field + " must not be blank"));
    }

    @Test
    void rejectsMalformedEmail() {
        Student student = validStudent();
        student.setEmail("invalid-address");
        assertThat(validator.validate(student)).singleElement().satisfies(violation -> {
            assertThat(violation.getPropertyPath().toString()).isEqualTo("email");
            assertThat(violation.getMessage()).isEqualTo("email must be a valid email address");
        });
    }
}
