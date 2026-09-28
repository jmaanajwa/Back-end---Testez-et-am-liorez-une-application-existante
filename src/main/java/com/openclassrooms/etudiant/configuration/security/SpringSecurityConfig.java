package com.openclassrooms.etudiant.configuration.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.openclassrooms.etudiant.service.JwtService;

/**
 * Configures JWT authentication and stateless HTTP endpoint security.
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Autowired
    private CustomUserDetailService customUserDetailService;

    /**
     * Combines user loading with BCrypt password verification.
     *
     * @return the authentication provider backed by persisted accounts
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Exposes the authentication manager built by Spring Security.
     *
     * @param authConfig the authentication configuration provided by Spring
     * @return the application authentication manager
     * @throws Exception if creating the manager fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Provides the encoder used to hash and verify passwords.
     *
     * @return the BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Builds a stateless security filter chain with JWT authentication.
     * Registration, login, and Actuator endpoints are public; all other endpoints require
     * authentication. Spring Security CORS and CSRF processing are disabled.
     *
     * @param http the HTTP security configuration
     * @param jwtService the JWT verification service
     * @return the security filter chain
     * @throws Exception if configuring the chain fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,JwtService jwtService) throws Exception {
       
         JwtAuthenticationFilter authenticationFilter =
            new JwtAuthenticationFilter(
                    jwtService,
                    customUserDetailService
            );
        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authorize -> authorize
                        // No auth needed on :
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/register", "/api/login").permitAll()
                        // Others protected routes will be added here.
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(
                        (request, response, exception) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
                        }));
        return http.build();
    }

}
