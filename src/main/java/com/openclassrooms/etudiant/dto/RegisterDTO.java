package com.openclassrooms.etudiant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Carries the required user registration details.
 */
@Data
public class RegisterDTO {
    /**
     * The nonblank first name of the user.
     */
    @NotBlank
    private String firstName;
    /**
     * The nonblank last name of the user.
     */
    @NotBlank
    private String lastName;
    /**
     * The requested nonblank login.
     */
    @NotBlank
    private String login;
    /**
     * The nonblank plaintext password to be hashed by the registration service.
     */
    @NotBlank
    private String password;

}
