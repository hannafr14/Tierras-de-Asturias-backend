package com.tierrasdeasturias.backend.controllers;

import com.tierrasdeasturias.backend.config.SecurityConfig;
import com.tierrasdeasturias.backend.dtos.LoginRequestDto;
import com.tierrasdeasturias.backend.entities.User;
import com.tierrasdeasturias.backend.exceptions.GlobalExceptionHandler;
import com.tierrasdeasturias.backend.repositories.UserRepository;
import com.tierrasdeasturias.backend.services.AuthService;
import com.tierrasdeasturias.backend.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tierrasdeasturias.backend.config.RememberMeTestConfig;

import jakarta.servlet.http.Cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;

@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        RememberMeTestConfig.class,
        GlobalExceptionHandler.class,
        AuthService.class,
        CustomUserDetailsService.class
})
public class AuthSessionTest {
    private static final String EMAIL = "ana@example.com";
    private static final String PASSWORD = "password123";
    private static final String INVALID_CREDENTIALS = "Invalid email or password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        User user = new User();
        setField(user, "id", 1L);
        user.setFirstName("Ana");
        user.setLastName("Garcia");
        user.setEmail(EMAIL);
        user.setPassword(passwordEncoder.encode(PASSWORD));

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    @Test
    void loginReturnsProfileAndPersistsAuthentication() throws Exception {
        CsrfState csrf = fetchCsrf(null);

        MvcResult result = mockMvc.perform(loginRequest(csrf, "ANA@example.com", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(EMAIL))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Garcia"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(EMAIL))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.lastName").value("Garcia"))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginRejectsWrongPasswordWithoutAuthenticating() throws Exception {
        CsrfState csrf = fetchCsrf(null);

        mockMvc.perform(loginRequest(csrf, EMAIL, "wrongpassword"))
                .andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
                .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));

        mockMvc.perform(get("/api/auth/me").session(csrf.session()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsUnknownEmailWithTheSameMessage() throws Exception {
        String unknownEmail = "unknown@example.com";
        when(userRepository.findByEmail(unknownEmail)).thenReturn(Optional.empty());
        CsrfState csrf = fetchCsrf(null);

        mockMvc.perform(loginRequest(csrf, unknownEmail, PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(unauthenticated())
                .andExpect(jsonPath("$.message").value(INVALID_CREDENTIALS));

        mockMvc.perform(get("/api/auth/me").session(csrf.session()))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @CsvSource({
            "'', password123",
            "invalid-email, password123",
            "ana@example.com, ''"
    })
    void loginRejectsInvalidDataBeforeLookingUpUser(String email, String password)
            throws Exception {
        CsrfState csrf = fetchCsrf(null);

        mockMvc.perform(loginRequest(csrf, email, password))
                .andExpect(status().isBadRequest())
                .andExpect(unauthenticated())
                .andExpect(jsonPath("$.message").value("Invalid request data"));

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void meRejectsRequestsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(unauthenticated());

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void loginRejectsRequestsWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(EMAIL, PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(unauthenticated());

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void loginChangesTheExistingSessionId() throws Exception {
        CsrfState csrf = fetchCsrf(null);
        String previousSessionId = csrf.session().getId();

        MvcResult result = mockMvc.perform(loginRequest(csrf, EMAIL, PASSWORD))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        assertNotEquals(previousSessionId, session.getId());
    }

    @Test
    void loginInvalidatesOldCsrfTokenAndAcceptsAFreshOne() throws Exception {
        CsrfState previousCsrf = fetchCsrf(null);

        MvcResult result = mockMvc.perform(loginRequest(previousCsrf, EMAIL, PASSWORD))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(loginRequest(previousCsrf, EMAIL, PASSWORD))
                .andExpect(status().isForbidden());

        CsrfState freshCsrf = fetchCsrf(session);

        mockMvc.perform(loginRequest(freshCsrf, EMAIL, PASSWORD))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(EMAIL));
    }

    @Test
    void rememberMeRestoresAuthenticationWithoutSession() throws Exception {
        CsrfState csrf = fetchCsrf(null);

        LoginRequestDto request = new LoginRequestDto();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setRememberMe(true);

        MvcResult result = mockMvc.perform(
                        loginRequest(csrf, EMAIL, PASSWORD)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("remember-me", true))
                .andExpect(cookie().maxAge("remember-me", 604800))
                .andReturn();

        Cookie rememberCookie = result.getResponse().getCookie("remember-me");
        assertNotNull(rememberCookie);

        MockHttpSession session =
                (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(session);
        session.invalidate();

        mockMvc.perform(get("/api/auth/me").cookie(rememberCookie))
                .andExpect(status().isOk())
                .andExpect(authenticated().withUsername(EMAIL))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    private CsrfState fetchCsrf(MockHttpSession session) throws Exception {
        MockHttpServletRequestBuilder request = get("/api/auth/csrf");
        if (session != null) {
            request.session(session);
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        MockHttpSession csrfSession = (MockHttpSession) result.getRequest().getSession(false);
        assertNotNull(csrfSession);
        var body = objectMapper.readTree(result.getResponse().getContentAsString());

        return new CsrfState(csrfSession, body.get("headerName").asText(), body.get("token").asText());
    }

    private MockHttpServletRequestBuilder loginRequest(CsrfState csrf, String email, String password)
            throws Exception {
        return post("/api/auth/login")
                .session(csrf.session())
                .header(csrf.headerName(), csrf.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson(email, password));
    }

    private String loginJson(String email, String password) throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail(email);
        request.setPassword(password);
        return objectMapper.writeValueAsString(request);
    }

    private record CsrfState(MockHttpSession session, String headerName, String token) {
    }
}

