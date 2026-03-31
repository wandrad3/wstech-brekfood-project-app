package com.br.wstech.brekfood.shared.interfaces.rest;

import com.br.wstech.brekfood.shared.domain.exception.BusinessRuleViolationException;
import com.br.wstech.brekfood.shared.domain.exception.EntityNotFoundException;
import com.br.wstech.brekfood.shared.interfaces.rest.exception.GlobalExceptionHandler;
import com.br.wstech.brekfood.shared.interfaces.rest.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandler_Test {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Nested
    @DisplayName("EntityNotFoundException")
    class HandleEntityNotFound {

        @Test
        @DisplayName("should return 404 with ENTITY_NOT_FOUND error code")
        void shouldReturn404() {
            EntityNotFoundException ex =
                    new EntityNotFoundException("Order", UUID.randomUUID());

            ResponseEntity<ApiError> response = handler.handleEntityNotFound(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(404);
            assertThat(response.getBody().getError()).isEqualTo("ENTITY_NOT_FOUND");
            assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
        }

        @Test
        @DisplayName("should include the exception message")
        void shouldIncludeExceptionMessage() {
            EntityNotFoundException ex = new EntityNotFoundException("Driver", "john@brekfood.com");

            ResponseEntity<ApiError> response = handler.handleEntityNotFound(ex, request);

            assertThat(response.getBody().getMessage()).isEqualTo("Driver not found: john@brekfood.com");
        }

        @Test
        @DisplayName("should set a non-null timestamp")
        void shouldSetTimestamp() {
            EntityNotFoundException ex = new EntityNotFoundException("Order", UUID.randomUUID());

            ResponseEntity<ApiError> response = handler.handleEntityNotFound(ex, request);

            assertThat(response.getBody().getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("BusinessRuleViolationException")
    class HandleBusinessRuleViolation {

        @Test
        @DisplayName("should return 422 with BUSINESS_RULE_VIOLATION error code")
        void shouldReturn422() {
            BusinessRuleViolationException ex =
                    new BusinessRuleViolationException("Invalid status transition");

            ResponseEntity<ApiError> response = handler.handleBusinessRuleViolation(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(response.getBody().getStatus()).isEqualTo(422);
            assertThat(response.getBody().getError()).isEqualTo("BUSINESS_RULE_VIOLATION");
        }

        @Test
        @DisplayName("should include the violation message")
        void shouldIncludeViolationMessage() {
            String message = "Restaurant is not accepting orders at this time";
            BusinessRuleViolationException ex = new BusinessRuleViolationException(message);

            ResponseEntity<ApiError> response = handler.handleBusinessRuleViolation(ex, request);

            assertThat(response.getBody().getMessage()).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("Generic Exception (fallback)")
    class HandleGenericException {

        @Test
        @DisplayName("should return 500 with INTERNAL_SERVER_ERROR for unexpected exceptions")
        void shouldReturn500() {
            Exception ex = new RuntimeException("Unexpected database failure");

            ResponseEntity<ApiError> response = handler.handleGenericException(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().getStatus()).isEqualTo(500);
            assertThat(response.getBody().getError()).isEqualTo("INTERNAL_SERVER_ERROR");
        }

        @Test
        @DisplayName("should NOT expose internal error details to the client")
        void shouldNotExposeInternalDetails() {
            Exception ex = new RuntimeException("SELECT * FROM users — raw DB error");

            ResponseEntity<ApiError> response = handler.handleGenericException(ex, request);

            assertThat(response.getBody().getMessage())
                    .doesNotContain("SELECT")
                    .isEqualTo("An unexpected error occurred. Please try again later.");
        }
    }
}

