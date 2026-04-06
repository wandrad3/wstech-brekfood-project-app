package com.br.wstech.brekfood.identity.infrastructure.security;

import com.br.wstech.brekfood.identity.domain.model.Role;
import com.br.wstech.brekfood.identity.domain.model.User;
import com.br.wstech.brekfood.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserDetailsServiceImpl}.
 *
 * <p>Verifies that the service correctly maps a {@link User} domain object
 * to Spring Security's {@link UserDetails}, including authority assignment
 * and account state propagation.
 */
@DisplayName("UserDetailsServiceImpl")
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImpl_Test {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private static final String EMAIL = "carol@brekfood.com";
    private static final String HASH  = "$2a$10$hashedPassword";

    // =========================================================================
    // loadUserByUsername — happy path
    // =========================================================================

    @Nested
    @DisplayName("loadUserByUsername() — user found")
    class WhenUserFound {

        @Test
        @DisplayName("should return UserDetails with matching email (username)")
        void shouldReturnUserDetailsWithEmail() {
            User user = User.create(EMAIL, HASH, "Carol", Role.CUSTOMER);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            UserDetails details = userDetailsService.loadUserByUsername(EMAIL);

            assertThat(details.getUsername()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("should return UserDetails with correct password hash")
        void shouldReturnUserDetailsWithPasswordHash() {
            User user = User.create(EMAIL, HASH, "Carol", Role.CUSTOMER);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            UserDetails details = userDetailsService.loadUserByUsername(EMAIL);

            assertThat(details.getPassword()).isEqualTo(HASH);
        }

        @Test
        @DisplayName("should grant ROLE_CUSTOMER authority for a CUSTOMER user")
        void shouldGrantCorrectAuthorityForCustomer() {
            User user = User.create(EMAIL, HASH, "Carol", Role.CUSTOMER);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            UserDetails details = userDetailsService.loadUserByUsername(EMAIL);

            assertThat(details.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_CUSTOMER");
        }

        @Test
        @DisplayName("should grant ROLE_DRIVER authority for a DRIVER user")
        void shouldGrantCorrectAuthorityForDriver() {
            User user = User.create("driver@brekfood.com", HASH, "Dave", Role.DRIVER);
            when(userRepository.findByEmail("driver@brekfood.com")).thenReturn(Optional.of(user));

            UserDetails details = userDetailsService.loadUserByUsername("driver@brekfood.com");

            assertThat(details.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_DRIVER");
        }

        @Test
        @DisplayName("should grant ROLE_ADMIN authority for an ADMIN user")
        void shouldGrantCorrectAuthorityForAdmin() {
            User user = User.create("admin@brekfood.com", HASH, "Admin", Role.ADMIN);
            when(userRepository.findByEmail("admin@brekfood.com")).thenReturn(Optional.of(user));

            UserDetails details = userDetailsService.loadUserByUsername("admin@brekfood.com");

            assertThat(details.getAuthorities())
                    .extracting(GrantedAuthority::getAuthority)
                    .containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should NOT lock account when user is active")
        void shouldNotLockActiveUser() {
            User user = User.create(EMAIL, HASH, "Carol", Role.CUSTOMER);
            // active = true by default after User.create
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            UserDetails details = userDetailsService.loadUserByUsername(EMAIL);

            assertThat(details.isEnabled()).isTrue();
            assertThat(details.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("should lock and disable account when user is inactive")
        void shouldLockInactiveUser() {
            User user = User.create(EMAIL, HASH, "Carol", Role.CUSTOMER);
            user.deactivate();   // sets active = false
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            UserDetails details = userDetailsService.loadUserByUsername(EMAIL);

            assertThat(details.isEnabled()).isFalse();
            assertThat(details.isAccountNonLocked()).isFalse();
        }

        @Test
        @DisplayName("should delegate to UserRepository.findByEmail")
        void shouldCallRepository() {
            User user = User.create(EMAIL, HASH, "Carol", Role.CUSTOMER);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            userDetailsService.loadUserByUsername(EMAIL);

            verify(userRepository).findByEmail(EMAIL);
        }
    }

    // =========================================================================
    // loadUserByUsername — user not found
    // =========================================================================

    @Nested
    @DisplayName("loadUserByUsername() — user not found")
    class WhenUserNotFound {

        @Test
        @DisplayName("should throw UsernameNotFoundException when email does not exist")
        void shouldThrowUsernameNotFoundException() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userDetailsService.loadUserByUsername(EMAIL))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining(EMAIL);
        }
    }
}

