package com.openclassrooms.etudiant.configuration.security;

import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailServiceTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final CustomUserDetailService service = new CustomUserDetailService(repository);

    @Test
    void loadsExistingAccount() {
        User user = new User();
        user.setLogin("alice");
        when(repository.findByLogin("alice")).thenReturn(Optional.of(user));
        assertThat(service.loadUserByUsername("alice")).isSameAs(user);
    }

    @Test
    void rejectsUnknownAccount() {
        when(repository.findByLogin("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
