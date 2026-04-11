package com.br.wstech.brekfood.identity.infrastructure.security;

import com.br.wstech.brekfood.identity.domain.model.User;
import com.br.wstech.brekfood.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Spring Security {@link UserDetailsService} implementation for the BrekFood platform.
 *
 * <p>Bridges the domain's {@link UserRepository} and Spring Security's authentication
 * infrastructure.  The "username" in this context is the user's e-mail address.
 *
 * <p><strong>Responsibilities:</strong>
 * <ul>
 *   <li>Load a {@link User} aggregate from the domain repository by e-mail</li>
 *   <li>Wrap it in a Spring Security {@link UserDetails} object with the correct authority</li>
 *   <li>Propagate account state ({@code active}) as lock/disable flags</li>
 * </ul>
 *
 * <p><strong>Usage:</strong> this service is auto-detected by Spring Security's
 * {@code DaoAuthenticationProvider} and by any component that injects
 * {@link UserDetailsService}.  It is NOT called during the stateless JWT flow
 * (handled by {@link JwtAuthenticationFilter}), but is available for future
 * authentication extensions (e.g., refresh-token flow, admin panel).
 *
 * <p><strong>Security note:</strong> the password stored in {@link UserDetails}
 * is the BCrypt hash — never the plain-text password.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their e-mail address.
     *
     * @param email the user's e-mail (used as the Spring Security "username")
     * @return a {@link UserDetails} wrapping the domain {@link User}
     * @throws UsernameNotFoundException if no user with the given e-mail exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.debug("UserDetailsService: no user found for email={}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });

        log.debug("UserDetailsService: loaded user email={}, active={}, role={}",
                user.getEmail(), user.isActive(), user.getRole());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .accountExpired(false)
                .accountLocked(!user.isActive())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}

