package com.br.wstech.brekfood.identity.interfaces.rest;

import com.br.wstech.brekfood.identity.application.command.LoginCommand;
import com.br.wstech.brekfood.identity.application.command.RegisterCommand;
import com.br.wstech.brekfood.identity.application.dto.AuthResponse;
import com.br.wstech.brekfood.identity.application.service.AuthService;
import com.br.wstech.brekfood.shared.interfaces.rest.response.ApiError;
import com.br.wstech.brekfood.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Identity & Auth use cases.
 *
 * <p>Exposes two public endpoints — no JWT required:
 * <ul>
 *   <li>{@code POST /api/v1/auth/register} — create a new account and receive a JWT</li>
 *   <li>{@code POST /api/v1/auth/login}    — verify credentials and receive a fresh JWT</li>
 * </ul>
 *
 * <p><strong>Responsibilities (this class only):</strong>
 * <ul>
 *   <li>Accept and validate the incoming request body with {@code @Valid}</li>
 *   <li>Delegate business logic to {@link AuthService}</li>
 *   <li>Wrap the result in the standard {@link ApiResponse} envelope</li>
 *   <li>Return the correct HTTP status code</li>
 * </ul>
 *
 * <p>No business logic, no direct repository/token access, no exception handling
 * (delegated to {@link com.br.wstech.brekfood.shared.interfaces.rest.exception.GlobalExceptionHandler}).
 */
@Tag(name = "Auth", description = "User registration and authentication (public endpoints)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // -------------------------------------------------------------------------
    // POST /api/v1/auth/register
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a new platform user account and immediately returns a signed JWT token.
                    
                    The token is valid and ready to use — no separate login step is required.
                    
                    **Roles:** `CUSTOMER`, `RESTAURANT_OWNER`, `DRIVER`, `ADMIN`
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully — JWT token returned",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed (blank fields, invalid e-mail format, password too short)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "E-mail address is already registered",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirements   // no Bearer token required for this public endpoint
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterCommand command) {
        return ApiResponse.of(authService.register(command));
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/auth/login
    // -------------------------------------------------------------------------

    @Operation(
            summary = "Authenticate a user",
            description = """
                    Validates the provided credentials and returns a fresh signed JWT token.
                    
                    Include the token in subsequent requests:
                    ```
                    Authorization: Bearer <token>
                    ```
                    
                    **Security note:** the error message is intentionally vague — it does NOT reveal
                    whether the e-mail exists or the password is wrong (prevents user enumeration).
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful — JWT token returned",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed (blank fields, invalid e-mail format)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid e-mail or password",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @SecurityRequirements   // no Bearer token required for this public endpoint
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginCommand command) {
        return ApiResponse.of(authService.login(command));
    }
}

