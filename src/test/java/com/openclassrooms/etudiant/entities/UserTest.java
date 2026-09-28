package com.openclassrooms.etudiant.entities;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.userdetails.UserDetails;
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

class UserTest {
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

    private User validUser() {
        return new User(null, "Lea", "Dupont", "lea", "password-hash", null, null);
    }

    @Test
    void acceptsValidUserBeforePersistenceGeneratesIdAndDates() {
        User user = validUser();
        assertThat(user.getId()).isNull();
        assertThat(user.getCreated_at()).isNull();
        assertThat(user.getUpdated_at()).isNull();
        assertThat(validator.validate(user)).isEmpty();
    }

    @Test
    void constructorPreservesAccountDetailsAndDates() {
        LocalDateTime created = LocalDateTime.of(2026, 1, 2, 10, 0);
        LocalDateTime updated = created.plusDays(1);
        User user = new User(7L, "Lea", "Dupont", "lea", "password-hash", created, updated);
        assertThat(user.getId()).isEqualTo(7L);
        assertThat(user.getFirstName()).isEqualTo("Lea");
        assertThat(user.getLastName()).isEqualTo("Dupont");
        assertThat(user.getLogin()).isEqualTo("lea");
        assertThat(user.getPassword()).isEqualTo("password-hash");
        assertThat(user.getCreated_at()).isEqualTo(created);
        assertThat(user.getUpdated_at()).isEqualTo(updated);
    }

    static Stream<Arguments> blankFields() {
        return Stream.of("firstName", "lastName", "login", "password")
                .flatMap(field -> Stream.of(null, "", "   ").map(value -> Arguments.of(field, value)));
    }

    @ParameterizedTest
    @MethodSource("blankFields")
    void rejectsEachMissingRequiredField(String field, String value) {
        User user = validUser();
        switch (field) {
            case "firstName" -> user.setFirstName(value);
            case "lastName" -> user.setLastName(value);
            case "login" -> user.setLogin(value);
            case "password" -> user.setPassword(value);
            default -> throw new IllegalArgumentException(field);
        }
        assertThat(validator.validate(user)).singleElement().satisfies(violation -> {
            assertThat(violation.getPropertyPath().toString()).isEqualTo(field);
            assertThat(violation.getConstraintDescriptor().getAnnotation()).isInstanceOf(NotBlank.class);
        });
    }

    @Test
    void exposesLoginAndPasswordToSpringSecurity() {
        User user = validUser();
        UserDetails details = user;
        assertThat(details.getUsername()).isEqualTo("lea");
        assertThat(details.getPassword()).isEqualTo("password-hash");
        user.setLogin("new-login");
        assertThat(details.getUsername()).isEqualTo("new-login");
    }

    @Test
    void accountIsActiveWithoutAssignedAuthorities() {
        UserDetails user = validUser();
        assertThat(user.getAuthorities()).isEmpty();
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
    }
}
