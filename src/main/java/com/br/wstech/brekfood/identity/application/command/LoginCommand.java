package com.br.wstech.brekfood.identity.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Command object carrying the credentials required to authenticate a user.
 *
 * <p>Validated at the interface layer (controller) with {@code @Valid}.
 * The application service receives an already-validated command.
 */
public record LoginCommand(

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email format is invalid")
        String email,

        @NotBlank(message = "Password must not be blank")
        String password
) {
}

