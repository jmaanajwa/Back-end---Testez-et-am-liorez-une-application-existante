package com.openclassrooms.etudiant.repository;

import com.openclassrooms.etudiant.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Provides JPA persistence and lookup operations for user accounts.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds an account by its login.
     *
     * @param login the login to look up
     * @return the matching user, or an empty result if no account matches
     */
    Optional<User> findByLogin(String login);
}
