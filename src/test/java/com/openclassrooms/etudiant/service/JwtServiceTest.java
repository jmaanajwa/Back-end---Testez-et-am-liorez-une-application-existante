package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    private final JwtService service = new JwtService();

    @Test
    void generatedTokenAuthenticatesItsOwner() {
        User user = new User();
        user.setLogin("alice");
        String token = service.generateToken(user);
        assertThat(token.split("\\.")).hasSize(3);
        assertThat(service.extractLogin(token)).isEqualTo("alice");
    }

    @Test
    void rejectsMalformedToken() {
        assertThatThrownBy(() -> service.extractLogin("invalid.token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsMissingToken() {
        assertThatThrownBy(() -> service.extractLogin(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.extractLogin("")).isInstanceOf(IllegalArgumentException.class);
    }
}
