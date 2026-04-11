package com.br.wstech.brekfood.identity.interfaces.rest;
import com.br.wstech.brekfood.AbstractIntegrationTest;
import com.br.wstech.brekfood.identity.application.command.LoginCommand;
import com.br.wstech.brekfood.identity.application.command.RegisterCommand;
import com.br.wstech.brekfood.identity.domain.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * Full-stack integration tests for the Auth endpoints.
 *
 * <p>Spins up the entire Spring context against a real PostgreSQL container
 * (via {@link AbstractIntegrationTest}) and exercises the complete auth flow:
 * register → login → use token on protected endpoint.
 *
 * <p>Flyway runs the real migrations (V0 + V1) against the container.
 */
@AutoConfigureMockMvc
@DisplayName("AuthController Integration")
class AuthControllerIntegration_Test extends AbstractIntegrationTest {
    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;
    private static final String REGISTER_URL  = "/api/v1/auth/register";
    private static final String LOGIN_URL      = "/api/v1/auth/login";
    private static final String PROTECTED_URL  = "/api/v1/customers/me";   // doesn't exist yet → 404 when authed
    // =========================================================================
    // Full register → login flow
    // =========================================================================
    @Nested
    @DisplayName("Register + Login flow")
    class RegisterAndLoginFlow {
        @Test
        @DisplayName("should register a new user and return a JWT token")
        void shouldRegisterAndReturnToken() throws Exception {
            RegisterCommand cmd = new RegisterCommand(
                    "integration_register@brekfood.com", "securepass1", "IT User", Role.CUSTOMER);
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cmd)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.email").value("integration_register@brekfood.com"))
                    .andExpect(jsonPath("$.data.name").value("IT User"))
                    .andExpect(jsonPath("$.data.userId").isNotEmpty())
                    .andExpect(jsonPath("$.data.expiresAt").isNotEmpty());
        }
        @Test
        @DisplayName("should login successfully after registration and return a fresh token")
        void shouldLoginAfterRegistration() throws Exception {
            // Register first
            RegisterCommand reg = new RegisterCommand(
                    "integration_login@brekfood.com", "securepass2", "Login User", Role.DRIVER);
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isCreated());
            // Then login
            LoginCommand login = new LoginCommand("integration_login@brekfood.com", "securepass2");
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.email").value("integration_login@brekfood.com"));
        }
        @Test
        @DisplayName("should return 422 when registering with a duplicate email")
        void shouldReturn422OnDuplicateEmail() throws Exception {
            RegisterCommand cmd = new RegisterCommand(
                    "duplicate@brekfood.com", "securepass3", "First", Role.CUSTOMER);
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cmd)))
                    .andExpect(status().isCreated());
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cmd)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"));
        }
        @Test
        @DisplayName("should return 401 when login password is wrong")
        void shouldReturn401OnWrongPassword() throws Exception {
            RegisterCommand reg = new RegisterCommand(
                    "wrongpass@brekfood.com", "rightpassword", "User", Role.CUSTOMER);
            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isCreated());
            LoginCommand login = new LoginCommand("wrongpass@brekfood.com", "wrongpassword");
            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
        }
    }
    // =========================================================================
    // Security filter: protected endpoints
    // =========================================================================
    @Nested
    @DisplayName("Security filter")
    class SecurityFilter {
        @Test
        @DisplayName("should return 401 when accessing protected endpoint without token")
        void shouldReturn401WithoutToken() throws Exception {
            mockMvc.perform(get(PROTECTED_URL))
                    .andExpect(status().isUnauthorized());
        }
        @Test
        @DisplayName("should return 401 when token is malformed")
        void shouldReturn401WithMalformedToken() throws Exception {
            mockMvc.perform(get(PROTECTED_URL)
                            .header("Authorization", "Bearer totally.invalid.token"))
                    .andExpect(status().isUnauthorized());
        }
        @Test
        @DisplayName("should pass security filter and reach the endpoint with a valid token")
        void shouldPassFilterWithValidToken() throws Exception {
            // Register to get a real token
            RegisterCommand reg = new RegisterCommand(
                    "jwt_test@brekfood.com", "securepass9", "JWT Tester", Role.CUSTOMER);
            MvcResult result = mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reg)))
                    .andExpect(status().isCreated())
                    .andReturn();
            String body = result.getResponse().getContentAsString();
            // Extract token from response body
            String token = objectMapper.readTree(body)
                    .at("/data/token").asText();
            assertThat(token).isNotBlank();
            // Protected URL doesn't exist yet → 404 (not 401/403) proves the filter passed
            mockMvc.perform(get(PROTECTED_URL)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }
}
