package com.br.wstech.brekfood.shared.infrastructure.config;

import com.br.wstech.brekfood.identity.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration for the BrekFood platform.
 *
 * <p>Policy:
 * <ul>
 *   <li>Stateless session (JWT, no HTTP session)</li>
 *   <li>CSRF disabled (REST API with token-based auth)</li>
 *   <li>CORS delegated to {@link WebConfig#corsConfigurationSource()}</li>
 *   <li>Auth endpoints and API docs are publicly accessible</li>
 *   <li>All other requests require a valid JWT</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Publicly accessible paths — no token required. */
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/actuator/health"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthFilter,
            CorsConfigurationSource corsConfigurationSource) throws Exception {

        http
            /*
             * CSRF is intentionally disabled — this is safe for the following reasons:
             *
             * 1. STATELESS sessions: SessionCreationPolicy.STATELESS is enforced below.
             *    The server never issues an HttpSession or a session cookie, so there is
             *    no session-bound credential for a malicious site to piggyback on.
             *
             * 2. Bearer token authentication: every authenticated request must carry an
             *    explicit `Authorization: Bearer <jwt>` header. Browsers do NOT include
             *    custom headers in cross-origin requests without an explicit CORS pre-flight
             *    — making CSRF structurally impossible.
             *
             * 3. No cookie-based auth: the API never sets authentication cookies.
             *    CSRF exploits the browser's automatic cookie sending; without cookies
             *    carrying credentials there is nothing to forge.
             *
             * References:
             *   - OWASP CSRF Prevention Cheat Sheet (Stateless / Token-Based Apps section)
             *   - Spring Security docs: "CSRF and REST" (recommends disabling for stateless APIs)
             */
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_PATHS).permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes the Spring Security {@link AuthenticationManager} as a bean.
     *
     * <p>Spring auto-configures a {@code DaoAuthenticationProvider} when a
     * {@link org.springframework.security.core.userdetails.UserDetailsService} bean
     * is present (our {@code UserDetailsServiceImpl}).  This bean is available for
     * future use cases that require programmatic authentication
     * (e.g., token-refresh endpoints, admin impersonation).
     *
     * @param config auto-injected {@link AuthenticationConfiguration}
     * @return the configured {@link AuthenticationManager}
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}

