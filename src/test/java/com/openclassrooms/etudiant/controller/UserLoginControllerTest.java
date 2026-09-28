package com.openclassrooms.etudiant.controller;

import com.openclassrooms.etudiant.handler.RestExceptionHandler;
import com.openclassrooms.etudiant.mapper.UserDtoMapper;
import com.openclassrooms.etudiant.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserLoginControllerTest {
    private final UserService service = mock(UserService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new UserController(service, mock(UserDtoMapper.class)))
            .setControllerAdvice(new RestExceptionHandler()).build();

    @Test
    void bindsCredentialsAndReturnsToken() throws Exception {
        when(service.login("alice", "secret")).thenReturn("signed-token");
        mvc.perform(post("/api/login").param("login", "alice").param("password", "secret"))
                .andExpect(status().isOk()).andExpect(content().string("signed-token"));
        verify(service).login("alice", "secret");
    }

    @Test
    void invalidCredentialsReturnBadRequestWithDetails() throws Exception {
        when(service.login("alice", "wrong")).thenThrow(new IllegalArgumentException("Invalid credentials"));
        mvc.perform(post("/api/login").param("login", "alice").param("password", "wrong"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.details").value("uri=/api/login"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
