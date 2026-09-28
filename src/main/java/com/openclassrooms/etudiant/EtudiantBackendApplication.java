package com.openclassrooms.etudiant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the student management Spring Boot application.
 */
@SpringBootApplication
public class EtudiantBackendApplication {

    /**
     * Starts the Spring context and the embedded HTTP server.
     *
     * @param args application startup arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(EtudiantBackendApplication.class, args);
    }

}
