package com.br.wstech.brekfood.identity.infrastructure.security;

import com.br.wstech.brekfood.identity.application.port.out.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Stateless JWT authentication filter.
 *
 * <p>Runs once per request (extends {@link OncePerRequestFilter}).
 * Extracts the {@code Authorization: Bearer <token>} header, validates the token,
 * and — if valid — populates the {@link SecurityContextHolder} so that downstream
 * filters and controllers see an authenticated principal.
 *
 * <p>Invalid or absent tokens are silently ignored here; endpoint protection
 * is enforced by the {@link com.br.wstech.brekfood.shared.infrastructure.config.SecurityConfig}
 * authorization rules.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX        = "Bearer ";

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (!tokenProvider.validateToken(token)) {
            log.debug("JWT validation failed for request: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String email = tokenProvider.extractEmail(token);
        String role  = tokenProvider.extractRole(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("JWT authenticated: email={}, role={}", email, role);

        filterChain.doFilter(request, response);
    }
}

