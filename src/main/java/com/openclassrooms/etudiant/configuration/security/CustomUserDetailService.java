package com.openclassrooms.etudiant.configuration.security;

import com.openclassrooms.etudiant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads persisted user accounts for Spring Security.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Finds a user by their login.
     *
     * @param login the login to look up
     * @return the matching account as {@link UserDetails}
     * @throws UsernameNotFoundException if no account matches the login
     */
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + login));
    }

}
