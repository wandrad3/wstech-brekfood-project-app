package com.br.wstech.brekfood.identity.interfaces.rest;

import com.br.wstech.brekfood.identity.application.command.LoginCommand;
import com.br.wstech.brekfood.identity.application.command.RegisterCommand;
import com.br.wstech.brekfood.identity.application.dto.AuthResponse;
import com.br.wstech.brekfood.identity.application.port.out.TokenProvider;
import com.br.wstech.brekfood.identity.application.service.AuthService;
import com.br.wstech.brekfood.identity.domain.exception.EmailAlreadyRegisteredException;
import com.br.wstech.brekfood.identity.domain.exception.InvalidCredentialsException;
import com.br.wstech.brekfood.identity.domain.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer slice test for {@link AuthController}.
 *
 * <p>Security filters are disabled ({@code addFilters = false}) because:
 * <ul>
 *   <li>Auth endpoints are public — security enforcement is not the subject of this test</li>
 *   <li>The full security stack (JWT filter, 401 enforcement) is validated by
 *       {@link AuthControllerIntegration_Test} against a real PostgreSQL container</li>
 * </ul>
 *
 * <p>This slice focuses on: request validation, service delegation,
 * response shape, and HTTP status codes.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("AuthController")
class AuthController_Test {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;
    @MockBean  private AuthService   authService;
    @MockBean  private TokenProvider tokenProvider;   // satisfies JwtAuthenticationFilter dependency

    private static final String  REGISTER_URL = "/api/v1/auth/register";
    private static final String  LOGIN_URL    = "/api/v1/auth/login";
    private static final UUID    USER_ID      = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String  TOKEN        = "eyJhbGciOiJIUzI1NiJ9.test.sig";
    private static final Instant EXPIRES      = Instant.parse("2026-12-31T23:59:59Z");

    private AuthResponse stub(String email, String name) {
        return new AuthResponse(TOKEN, EXPIRES, USER_ID, email, name, Role.CUSTOMER.getDisplayName());
    }

    // =========================================================================
    // POST /api/v1/auth/register
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class Register {

        @Test
        @DisplayName("201 when registration succeeds")
        void should201OnSuccess() throws Exception {
            when(authService.register(any(RegisterCommand.class))).thenReturn(stub("a@b.com", "Alice"));

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new RegisterCommand("a@b.com", "password123", "Alice", Role.CUSTOMER))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.token").value(TOKEN))
                    .andExpect(jsonPath("$.data.email").value("a@b.com"))
                    .andExpect(jsonPath("$.data.name").value("Alice"))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID.toString()));
        }

        @Test
        @DisplayName("400 when email is blank")
        void should400WhenEmailBlank() throws Exception {
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new RegisterCommand("", "password123", "Alice", Role.CUSTOMER))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
        }

        @Test
        @DisplayName("400 when email format is invalid")
        void should400WhenEmailInvalid() throws Exception {
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new RegisterCommand("not-an-email", "password123", "Alice", Role.CUSTOMER))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
        }

        @Test
        @DisplayName("400 when password is too short (< 8 chars)")
        void should400WhenPasswordTooShort() throws Exception {
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new RegisterCommand("a@b.com", "short", "Alice", Role.CUSTOMER))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("password"));
        }

        @Test
        @DisplayName("400 when name is blank")
        void should400WhenNameBlank() throws Exception {
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new RegisterCommand("a@b.com", "password123", "", Role.CUSTOMER))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
        }

        @Test
        @DisplayName("422 when email is already registered")
        void should422WhenEmailTaken() throws Exception {
            when(authService.register(any())).thenThrow(new EmailAlreadyRegisteredException("a@b.com"));

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new RegisterCommand("a@b.com", "password123", "Alice", Role.CUSTOMER))))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"))
                    .andExpect(jsonPath("$.message").value("E-mail already registered: a@b.com"));
        }
    }

    // =========================================================================
    // POST /api/v1/auth/login
    // =========================================================================

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("200 when credentials are valid")
        void should200OnSuccess() throws Exception {
            when(authService.login(any(LoginCommand.class))).thenReturn(stub("a@b.com", "Alice"));

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginCommand("a@b.com", "password123"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.token").value(TOKEN))
                    .andExpect(jsonPath("$.data.email").value("a@b.com"));
        }

        @Test
        @DisplayName("400 when email is blank")
        void should400WhenEmailBlank() throws Exception {
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginCommand("", "password123"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
        }

        @Test
        @DisplayName("400 when password is blank")
        void should400WhenPasswordBlank() throws Exception {
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginCommand("a@b.com", ""))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("password"));
        }

        @Test
        @DisplayName("401 when credentials are invalid")
        void should401WhenCredentialsInvalid() throws Exception {
            when(authService.login(any())).thenThrow(new InvalidCredentialsException());

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new LoginCommand("a@b.com", "wrongpass"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
        }
    }
}