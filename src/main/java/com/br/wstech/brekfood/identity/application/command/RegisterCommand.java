package com.br.wstech.brekfood.identity.application.command;

import com.br.wstech.brekfood.identity.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Command object carrying the data required to register a new platform user.
 *
 * <p>Validated at the interface layer (controller) with {@code @Valid}.
 * The application service receives an already-validated command.
 *
 * <p>Password is stored as plain-text in transit only; the application service
 * is responsible for encoding it via {@code PasswordEncoder} before persisting.
 */
public record RegisterCommand(

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email format is invalid")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Name must not be blank")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotNull(message = "Role must not be null")
        Role role
) {
}

