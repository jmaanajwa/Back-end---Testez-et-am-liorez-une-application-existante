package com.openclassrooms.etudiant.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.etudiant.dto.LoginRequestDTO;
import com.openclassrooms.etudiant.dto.RegisterDTO;
import com.openclassrooms.etudiant.entities.Student;
import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.handler.ErrorDetails;
import java.beans.Introspector;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.*;

class ModelContractTest {
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static List<Class<?>> modelTypes() {
        return List.of(LoginRequestDTO.class, RegisterDTO.class, Student.class, User.class, ErrorDetails.class);
    }

    private Object sample(Class<?> type) throws Exception {
        Object value = type.getDeclaredConstructor().newInstance();
        for (var property : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
            if (property.getWriteMethod() != null) {
                Object fieldValue = property.getPropertyType() == Long.class ? 7L
                        : property.getPropertyType() == LocalDateTime.class ? LocalDateTime.of(2026, 1, 2, 12, 30)
                        : property.getName() + "-value";
                property.getWriteMethod().invoke(value, fieldValue);
            }
        }
        return value;
    }

    @ParameterizedTest
    @MethodSource("modelTypes")
    void jsonRoundTripPreservesEveryWritableProperty(Class<?> type) throws Exception {
        Object original = sample(type);
        Object restored = mapper.readValue(mapper.writeValueAsString(original), type);
        for (var property : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
            if (property.getWriteMethod() != null) {
                assertThat(property.getReadMethod().invoke(restored))
                        .as(property.getName()).isEqualTo(property.getReadMethod().invoke(original));
            }
        }
        assertThat(restored).isEqualTo(original);
        assertThat(restored.hashCode()).isEqualTo(original.hashCode());
    }

    @ParameterizedTest
    @MethodSource("modelTypes")
    void equalitySupportsIdentitySymmetryAndHashCollections(Class<?> type) throws Exception {
        Object first = sample(type);
        Object second = sample(type);
        Object third = sample(type);
        assertThat(first).isEqualTo(first).isEqualTo(second);
        assertThat(second).isEqualTo(first).isEqualTo(third);
        assertThat(first).isEqualTo(third).isNotEqualTo(null).isNotEqualTo(new Object());
        var values = new HashSet<Object>();
        values.add(first);
        values.add(second);
        assertThat(values).hasSize(1).contains(third);
        assertThat(first.toString()).contains(type.getSimpleName());
    }

    @ParameterizedTest
    @MethodSource("modelTypes")
    void emptyModelsHaveConsistentEqualityAndHashCode(Class<?> type) throws Exception {
        Object first = type.getDeclaredConstructor().newInstance();
        Object second = type.getDeclaredConstructor().newInstance();
        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @ParameterizedTest
    @MethodSource("modelTypes")
    void eachWritablePropertyParticipatesInEquality(Class<?> type) throws Exception {
        for (var property : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
            if (property.getWriteMethod() == null) continue;
            Object original = sample(type);
            Object modified = sample(type);
            Object replacement = property.getPropertyType() == Long.class ? 8L
                    : property.getPropertyType() == LocalDateTime.class ? LocalDateTime.of(2027, 1, 2, 12, 30)
                    : "different-value";
            property.getWriteMethod().invoke(modified, replacement);
            assertThat(original).as(property.getName()).isNotEqualTo(modified);
            assertThat(modified).as(property.getName()).isNotEqualTo(original);
            property.getWriteMethod().invoke(modified, new Object[]{null});
            assertThat(original).as(property.getName()).isNotEqualTo(modified);
            assertThat(modified).as(property.getName()).isNotEqualTo(original);
        }
    }
}
