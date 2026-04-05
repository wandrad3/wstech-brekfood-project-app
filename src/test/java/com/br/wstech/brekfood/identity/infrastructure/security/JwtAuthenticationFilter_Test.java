package com.br.wstech.brekfood.identity.infrastructure.security;

import com.br.wstech.brekfood.identity.application.port.out.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("JwtAuthenticationFilter")
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilter_Test {

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest  request;
    private MockHttpServletResponse response;
    private MockFilterChain         chain;

    @BeforeEach
    void setUp() {
        request  = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain    = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    // -------------------------------------------------------------------------
    // No / malformed header
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("when Authorization header is absent or malformed")
    class NoHeader {

        @Test
        @DisplayName("no header — should not set authentication and should pass through")
        void noHeaderPassesThrough() throws Exception {
            filter.doFilterInternal(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(tokenProvider, never()).validateToken(org.mockito.ArgumentMatchers.any());
        }

        @Test
        @DisplayName("non-Bearer header — should not set authentication")
        void nonBearerHeaderPassesThrough() throws Exception {
            request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

            filter.doFilterInternal(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // Invalid token
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("when token is invalid")
    class InvalidToken {

        @Test
        @DisplayName("should not set authentication and should pass through")
        void invalidTokenPassesThrough() throws Exception {
            request.addHeader("Authorization", "Bearer bad.token.value");
            when(tokenProvider.validateToken("bad.token.value")).thenReturn(false);

            filter.doFilterInternal(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(tokenProvider, never()).extractEmail(org.mockito.ArgumentMatchers.any());
        }
    }

    // -------------------------------------------------------------------------
    // Valid token
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("when token is valid")
    class ValidToken {

        private static final String TOKEN = "valid.jwt.token";
        private static final String EMAIL = "alice@brekfood.com";
        private static final String ROLE  = "CUSTOMER";

        @BeforeEach
        void stubTokenProvider() {
            when(tokenProvider.validateToken(TOKEN)).thenReturn(true);
            when(tokenProvider.extractEmail(TOKEN)).thenReturn(EMAIL);
            when(tokenProvider.extractRole(TOKEN)).thenReturn(ROLE);
        }

        @Test
        @DisplayName("should set UsernamePasswordAuthenticationToken in SecurityContext")
        void shouldSetAuthentication() throws Exception {
            request.addHeader("Authorization", "Bearer " + TOKEN);

            filter.doFilterInternal(request, response, chain);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getPrincipal()).isEqualTo(EMAIL);
            assertThat(auth.isAuthenticated()).isTrue();
        }

        @Test
        @DisplayName("should grant authority ROLE_<role>")
        void shouldGrantCorrectAuthority() throws Exception {
            request.addHeader("Authorization", "Bearer " + TOKEN);

            filter.doFilterInternal(request, response, chain);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth.getAuthorities())
                    .extracting(a -> a.getAuthority())
                    .containsExactly("ROLE_" + ROLE);
        }

        @Test
        @DisplayName("should continue the filter chain")
        void shouldContinueFilterChain() throws Exception {
            request.addHeader("Authorization", "Bearer " + TOKEN);

            filter.doFilterInternal(request, response, chain);

            assertThat(chain.getRequest()).isNotNull();
        }
    }
}

