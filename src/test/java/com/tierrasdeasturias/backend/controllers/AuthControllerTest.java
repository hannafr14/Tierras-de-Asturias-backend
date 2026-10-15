package com.tierrasdeasturias.backend.controllers;

import com.tierrasdeasturias.backend.config.SecurityConfig;
import com.tierrasdeasturias.backend.dtos.AuthResponseDto;
import com.tierrasdeasturias.backend.dtos.RegisterRequestDto;
import com.tierrasdeasturias.backend.exceptions.DuplicateEmailException;
import com.tierrasdeasturias.backend.exceptions.GlobalExceptionHandler;
import com.tierrasdeasturias.backend.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @MockitoBean
    private AuthService authService;

    private RegisterRequestDto createValidRegisterRequest() {
        RegisterRequestDto request = new RegisterRequestDto();

        request.setFirstName("Ana");
        request.setLastName("Garcia");
        request.setEmail("ana@example.com");
        request.setPassword("password123");

        return request;
    }

    @Test
    void registerReturnsCreatedUser() throws Exception {
        AuthResponseDto response = new AuthResponseDto(
                1L,
                "Ana",
                "Garcia",
                "ana@example.com"
        );

        when(authService.register(any(RegisterRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRegisterRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Garcia"))
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void registerReturnsConflictForDuplicateEmail() throws Exception {
        when(authService.register(any(RegisterRequestDto.class)))
                .thenThrow(new DuplicateEmailException("Email is already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRegisterRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void registerReturnsBadRequestForInvalidData() throws Exception {
        RegisterRequestDto request = createValidRegisterRequest();

        request.setEmail("invalid-email");
        request.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request data"));

        verify(authService, never()).register(any(RegisterRequestDto.class));
    }
}