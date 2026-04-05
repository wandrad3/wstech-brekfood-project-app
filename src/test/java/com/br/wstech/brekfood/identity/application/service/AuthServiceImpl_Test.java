package com.br.wstech.brekfood.identity.application.service;

import com.br.wstech.brekfood.identity.application.command.LoginCommand;
import com.br.wstech.brekfood.identity.application.command.RegisterCommand;
import com.br.wstech.brekfood.identity.application.dto.AuthResponse;
import com.br.wstech.brekfood.identity.application.port.out.TokenDetails;
import com.br.wstech.brekfood.identity.application.port.out.TokenProvider;
import com.br.wstech.brekfood.identity.domain.exception.EmailAlreadyRegisteredException;
import com.br.wstech.brekfood.identity.domain.exception.InvalidCredentialsException;
import com.br.wstech.brekfood.identity.domain.model.Role;
import com.br.wstech.brekfood.identity.domain.model.User;
import com.br.wstech.brekfood.identity.domain.repository.UserRepository;
import com.br.wstech.brekfood.shared.domain.model.BaseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("AuthServiceImpl")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImpl_Test {

    @Mock private UserRepository  userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenProvider   tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String EMAIL   = "alice@brekfood.com";
    private static final String PASSWORD = "password123";
    private static final String HASH    = "$2a$10$hashedPassword";
    private static final String TOKEN   = "eyJhbGciOiJIUzI1NiJ9.test.sig";
    private static final UUID   USER_ID = UUID.randomUUID();

    /** Injects a known UUID via reflection (simulates JPA @GeneratedValue). */
    private static User userWithId(String email, String hash, String name, Role role) {
        User user = User.create(email, hash, name, role);
        try {
            Field idField = BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, USER_ID);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set user id via reflection", e);
        }
        return user;
    }

    private TokenDetails stubTokenDetails() {
        return new TokenDetails(TOKEN, Instant.now().plusSeconds(3600));
    }

    // -------------------------------------------------------------------------
    // register()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("register()")
    class Register {

        @BeforeEach
        void setUp() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(HASH);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                try {
                    Field idField = BaseEntity.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(u, USER_ID);
                } catch (Exception e) { throw new RuntimeException(e); }
                return u;
            });
            when(tokenProvider.generateToken(any(User.class))).thenReturn(stubTokenDetails());
        }

        @Test
        @DisplayName("happy path - should return AuthResponse with token and user info")
        void shouldReturnAuthResponseWithToken() {
            RegisterCommand cmd = new RegisterCommand(EMAIL, PASSWORD, "Alice", Role.CUSTOMER);

            AuthResponse response = authService.register(cmd);

            assertThat(response.token()).isEqualTo(TOKEN);
            assertThat(response.email()).isEqualTo(EMAIL);
            assertThat(response.name()).isEqualTo("Alice");
            assertThat(response.roleDisplayName()).isEqualTo(Role.CUSTOMER.getDisplayName());
            assertThat(response.userId()).isEqualTo(USER_ID);
            assertThat(response.expiresAt()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("should encode password before persisting")
        void shouldEncodePassword() {
            RegisterCommand cmd = new RegisterCommand(EMAIL, PASSWORD, "Alice", Role.CUSTOMER);
            authService.register(cmd);

            verify(passwordEncoder).encode(PASSWORD);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPasswordHash()).isEqualTo(HASH);
        }

        @Test
        @DisplayName("should throw EmailAlreadyRegisteredException when email is taken")
        void shouldThrowWhenEmailTaken() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);
            RegisterCommand cmd = new RegisterCommand(EMAIL, PASSWORD, "Alice", Role.CUSTOMER);

            assertThatThrownBy(() -> authService.register(cmd))
                    .isInstanceOf(EmailAlreadyRegisteredException.class)
                    .hasMessageContaining(EMAIL);

            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    // -------------------------------------------------------------------------
    // login()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("login()")
    class Login {

        private User existingUser;

        @BeforeEach
        void setUp() {
            existingUser = userWithId(EMAIL, HASH, "Alice", Role.CUSTOMER);
            when(tokenProvider.generateToken(any(User.class))).thenReturn(stubTokenDetails());
        }

        @Test
        @DisplayName("happy path - should return AuthResponse with token")
        void shouldReturnAuthResponseWithToken() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);

            AuthResponse response = authService.login(new LoginCommand(EMAIL, PASSWORD));

            assertThat(response.token()).isEqualTo(TOKEN);
            assertThat(response.email()).isEqualTo(EMAIL);
            assertThat(response.userId()).isEqualTo(USER_ID);
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException when email not found")
        void shouldThrowWhenEmailNotFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(new LoginCommand(EMAIL, PASSWORD)))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage(InvalidCredentialsException.MESSAGE);

            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException when password is wrong")
        void shouldThrowWhenPasswordWrong() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches("wrongPass", HASH)).thenReturn(false);

            assertThatThrownBy(() -> authService.login(new LoginCommand(EMAIL, "wrongPass")))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage(InvalidCredentialsException.MESSAGE);

            verify(tokenProvider, never()).generateToken(any());
        }
    }
}

