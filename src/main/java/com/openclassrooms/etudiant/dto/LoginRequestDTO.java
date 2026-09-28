package com.openclassrooms.etudiant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Carries login credentials bound from HTTP form or query parameters.
 */
@Data
public class LoginRequestDTO {
    /**
     * The account login.
     */
    private String login;
    /**
     * The plaintext password to verify during authentication.
     */
    private String password;

}
