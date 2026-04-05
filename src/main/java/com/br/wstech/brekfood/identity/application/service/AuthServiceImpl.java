package com.br.wstech.brekfood.identity.application.service;

import com.br.wstech.brekfood.identity.application.command.LoginCommand;
import com.br.wstech.brekfood.identity.application.command.RegisterCommand;
import com.br.wstech.brekfood.identity.application.dto.AuthResponse;
import com.br.wstech.brekfood.identity.application.port.out.TokenDetails;
import com.br.wstech.brekfood.identity.application.port.out.TokenProvider;
import com.br.wstech.brekfood.identity.domain.exception.EmailAlreadyRegisteredException;
import com.br.wstech.brekfood.identity.domain.exception.InvalidCredentialsException;
import com.br.wstech.brekfood.identity.domain.model.User;
import com.br.wstech.brekfood.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link AuthService}.
 *
 * <p>Orchestrates the register and login use cases by delegating to:
 * <ul>
 *   <li>{@link UserRepository}  — persistence port (domain interface)</li>
 *   <li>{@link PasswordEncoder} — BCrypt hashing (Spring Security)</li>
 *   <li>{@link TokenProvider}   — JWT generation (infrastructure port)</li>
 * </ul>
 *
 * <p>This class contains <em>no</em> framework-specific JWT details — all token
 * operations go through the {@link TokenProvider} output port.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    // -------------------------------------------------------------------------
    // Use cases
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Flow:
     * <ol>
     *   <li>Check e-mail uniqueness → throw {@link EmailAlreadyRegisteredException} if taken</li>
     *   <li>Encode the plain-text password with BCrypt</li>
     *   <li>Create and persist the {@link User} aggregate via its factory method</li>
     *   <li>Generate a JWT and return an {@link AuthResponse}</li>
     * </ol>
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyRegisteredException(command.email());
        }

        String passwordHash = passwordEncoder.encode(command.password());
        User user = User.create(command.email(), passwordHash, command.name(), command.role());
        userRepository.save(user);

        log.info("User registered: id={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());

        return buildAuthResponse(user);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Flow:
     * <ol>
     *   <li>Look up user by e-mail → throw {@link InvalidCredentialsException} if absent
     *       (same exception for e-mail-not-found and wrong-password to prevent enumeration)</li>
     *   <li>Verify the plain-text password against the stored BCrypt hash</li>
     *   <li>Generate a JWT and return an {@link AuthResponse}</li>
     * </ol>
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        log.info("User authenticated: id={}, email={}", user.getId(), user.getEmail());

        return buildAuthResponse(user);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private AuthResponse buildAuthResponse(User user) {
        TokenDetails tokenDetails = tokenProvider.generateToken(user);
        return new AuthResponse(
                tokenDetails.token(),
                tokenDetails.expiresAt(),
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().getDisplayName()
        );
    }
}

