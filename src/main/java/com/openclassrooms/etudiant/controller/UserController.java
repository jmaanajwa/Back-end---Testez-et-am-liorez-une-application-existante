package com.openclassrooms.etudiant.controller;

import com.openclassrooms.etudiant.dto.LoginRequestDTO;
import com.openclassrooms.etudiant.dto.RegisterDTO;
import com.openclassrooms.etudiant.mapper.UserDtoMapper;
import com.openclassrooms.etudiant.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes public user registration and login endpoints.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserDtoMapper userDtoMapper;

    /**
     * Registers a user from a validated JSON request body.
     *
     * @param registerDTO the registration details, including the plaintext password
     * @return an HTTP 201 response without content
     * @throws IllegalArgumentException if the login is already in use
     */
    @PostMapping("/api/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        userService.register(userDtoMapper.toEntity(registerDTO));
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Authenticates a user using form or query parameters.
     * Spring binds the DTO without {@code @RequestBody}; this method does not read a JSON body.
     *
     * @param loginRequestDTO the login and password
     * @return an HTTP 200 response containing the JWT as plain text
     * @throws IllegalArgumentException if credentials are missing or incorrect
     */
    @PostMapping("/api/login")
    public ResponseEntity<?> login(LoginRequestDTO loginRequestDTO) {
        String jwtToken = userService.login(loginRequestDTO.getLogin(), loginRequestDTO.getPassword());
        return ResponseEntity.ok(jwtToken);
    }


}
