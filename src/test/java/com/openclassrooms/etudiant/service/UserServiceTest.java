package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LOGIN = "LOGIN";
    private static final String PASSWORD = "PASSWORD";
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private UserService userService;

    @Test
    public void test_create_null_user_throws_IllegalArgumentException() {
        // GIVEN

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register(null));
    }

    @Test
    public void test_create_already_exist_user_throws_IllegalArgumentException() {
        // GIVEN
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD);
        when(userRepository.findByLogin(any())).thenReturn(Optional.of(user));

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register(user));
    }

    @Test
    public void test_create_user() {
        // GIVEN
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD);
        when(userRepository.findByLogin(any())).thenReturn(Optional.empty());

        // WHEN
        userService.register(user);

        // THEN
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue()).isEqualTo(user);
    }

    @Test
    void loginReturnsTokenForMatchingPassword() {
        User user = new User();
        user.setPassword("stored-hash");
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, "stored-hash")).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("signed-token");

        assertThat(userService.login(LOGIN, PASSWORD)).isEqualTo("signed-token");
        ArgumentCaptor<org.springframework.security.core.userdetails.UserDetails> captor =
                ArgumentCaptor.forClass(org.springframework.security.core.userdetails.UserDetails.class);
        verify(jwtService).generateToken(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo(LOGIN);
    }

    @Test
    void loginRejectsWrongPasswordWithoutIssuingToken() {
        User user = new User();
        user.setPassword("stored-hash");
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, "stored-hash")).thenReturn(false);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> userService.login(LOGIN, PASSWORD))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid credentials");
        org.mockito.Mockito.verifyNoInteractions(jwtService);
    }

    @Test
    void loginRejectsUnknownUser() {
        when(userRepository.findByLogin(LOGIN)).thenReturn(Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> userService.login(LOGIN, PASSWORD))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Invalid credentials");
        org.mockito.Mockito.verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void loginRejectsNullCredentialsBeforeAccessingRepository() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.login(null, PASSWORD));
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.login(LOGIN, null));
        org.mockito.Mockito.verifyNoInteractions(userRepository, passwordEncoder, jwtService);
    }
}
