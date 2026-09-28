package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * Manages user registration and password authentication.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Registers an account after checking the login and hashing the password.
     * The password of the supplied entity is replaced with its hash before saving.
     *
     * @param user the user to register with a plaintext password
     * @throws IllegalArgumentException if the user or password is null,
     *     or if the login is already in use
     */
    public void register(User user) {
        Assert.notNull(user, "User must not be null");
        log.info("Registering new user");

        Optional<User> optionalUser = userRepository.findByLogin(user.getLogin());
        if (optionalUser.isPresent()) {
            throw new IllegalArgumentException("User with login " + user.getLogin() + " already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    /**
     * Verifies the account password and generates an authentication token.
     *
     * @param login the account login
     * @param password the plaintext password to verify
     * @return a signed JWT for the authenticated user
     * @throws IllegalArgumentException if either parameter is null or credentials are incorrect
     */
    public String login(String login, String password) {
        Assert.notNull(login, "Login must not be null");
        Assert.notNull(password, "Password must not be null");
        Optional<User> user = userRepository.findByLogin(login);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(login)
                    .password(password)
                    .build();
            return jwtService.generateToken(userDetails);
        } else {
            throw new IllegalArgumentException("Invalid credentials");
        }
    }


}
