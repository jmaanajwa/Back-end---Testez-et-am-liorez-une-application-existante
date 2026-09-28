package com.openclassrooms.etudiant.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Describes an HTTP error with its timestamp, message, and request context.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {
    /**
     * The local timestamp when the error response was created.
     */
    private LocalDateTime timestamp;
    /**
     * The message of the exception that caused the error.
     */
    private String message;
    /**
     * The description of the affected request, excluding client information.
     */
    private String details;
}
