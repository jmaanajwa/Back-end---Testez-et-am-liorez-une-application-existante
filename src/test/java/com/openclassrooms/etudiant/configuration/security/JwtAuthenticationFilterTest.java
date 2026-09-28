package com.openclassrooms.etudiant.configuration.security;

import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private final JwtService jwtService = mock(JwtService.class);
    private final CustomUserDetailService users = mock(CustomUserDetailService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, users);
    private final FilterChain chain = mock(FilterChain.class);
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"Basic credentials", "Bearer"})
    void continuesWithoutBearerToken(String header) throws Exception {
        if (header != null) request.addHeader("Authorization", header);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService, users);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void authenticatesValidTokenAndContinuesChain() throws Exception {
        request.addHeader("Authorization", "bEaReR   token  ");
        when(jwtService.extractLogin("token")).thenReturn("alice");
        User user = new User();
        user.setLogin("alice");
        when(users.loadUserByUsername("alice")).thenReturn(user);
        filter.doFilter(request, response, chain);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isSameAs(user);
        assertThat(authentication.getName()).isEqualTo("alice");
        assertThat(authentication.getAuthorities()).isEmpty();
        assertThat(authentication.getDetails()).isNotNull();
        verify(chain).doFilter(request, response);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void rejectsMissingSubject(String login) throws Exception {
        request.addHeader("Authorization", "Bearer token");
        when(jwtService.extractLogin("token")).thenReturn(login);
        filter.doFilter(request, response, chain);
        assertRejected();
        verifyNoInteractions(users);
    }

    @Test
    void rejectsInvalidTokenAndClearsExistingAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("previous", "password"));
        request.addHeader("Authorization", "Bearer token");
        when(jwtService.extractLogin("token")).thenThrow(new JwtException("Invalid signature"));
        filter.doFilter(request, response, chain);
        assertRejected();
        verifyNoInteractions(users);
    }

    @Test
    void rejectsUnknownUser() throws Exception {
        request.addHeader("Authorization", "Bearer token");
        when(jwtService.extractLogin("token")).thenReturn("missing");
        when(users.loadUserByUsername("missing")).thenThrow(new UsernameNotFoundException("missing"));
        filter.doFilter(request, response, chain);
        assertRejected();
    }

    @Test
    void rejectsDisabledAccount() throws Exception {
        request.addHeader("Authorization", "Bearer token");
        when(jwtService.extractLogin("token")).thenReturn("alice");
        when(users.loadUserByUsername("alice")).thenReturn(
                org.springframework.security.core.userdetails.User.withUsername("alice")
                        .password("hash").disabled(true).authorities("USER").build());
        filter.doFilter(request, response, chain);
        assertRejected();
    }

    private void assertRejected() {
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(chain);
    }
}
