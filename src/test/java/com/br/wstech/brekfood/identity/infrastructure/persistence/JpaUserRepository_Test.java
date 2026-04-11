package com.br.wstech.brekfood.identity.infrastructure.persistence;

import com.br.wstech.brekfood.identity.domain.model.Role;
import com.br.wstech.brekfood.identity.domain.model.User;
import com.br.wstech.brekfood.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.br.wstech.brekfood.shared.infrastructure.config.JpaConfig;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice test for {@link JpaUserRepository} using H2 in-memory database.
 *
 * <p>{@link DataJpaTest} loads only the JPA slice:
 * entity classes, repositories, and {@link TestEntityManager}.
 * No full Spring context is started — fast and isolated.
 *
 * <p>The {@link JpaUserRepository} adapter is imported explicitly because
 * {@link DataJpaTest} does not scan {@code @Repository} components by default
 * (only Spring Data interfaces are auto-configured).
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({JpaUserRepository.class, JpaConfig.class})
@DisplayName("JpaUserRepository")
class JpaUserRepository_Test {

    @Autowired
    private UserRepository userRepository;   // the domain port — injected via adapter

    @Autowired
    private TestEntityManager entityManager;

    private static final String EMAIL    = "bob@brekfood.com";
    private static final String HASH     = "$2a$10$fixedHashForTests";
    private static final String NAME     = "Bob";
    private static final Role   ROLE     = Role.CUSTOMER;

    /** Helper: persists a User directly via EntityManager (bypassing adapter). */
    private User persistUser(String email, String name, Role role) {
        User user = User.create(email, HASH, name, role);
        entityManager.persistAndFlush(user);
        return user;
    }

    // =========================================================================
    // save()
    // =========================================================================

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("should persist a new user and assign a UUID")
        void shouldPersistAndAssignId() {
            User user = User.create(EMAIL, HASH, NAME, ROLE);

            User saved = userRepository.save(user);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getEmail()).isEqualTo(EMAIL);
            assertThat(saved.getName()).isEqualTo(NAME);
            assertThat(saved.getRole()).isEqualTo(ROLE);
            assertThat(saved.isActive()).isTrue();
        }

        @Test
        @DisplayName("should persist createdAt and updatedAt timestamps")
        void shouldPersistAuditTimestamps() {
            User user = User.create(EMAIL, HASH, NAME, ROLE);
            User saved = userRepository.save(user);
            entityManager.flush();

            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }
    }

    // =========================================================================
    // findById()
    // =========================================================================

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return user when ID exists")
        void shouldReturnUserWhenFound() {
            User persisted = persistUser(EMAIL, NAME, ROLE);

            Optional<User> result = userRepository.findById(persisted.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(EMAIL);
        }

        @Test
        @DisplayName("should return empty when ID does not exist")
        void shouldReturnEmptyWhenNotFound() {
            Optional<User> result = userRepository.findById(UUID.randomUUID());

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // findByEmail()
    // =========================================================================

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("should return user when email exists")
        void shouldReturnUserWhenFound() {
            persistUser(EMAIL, NAME, ROLE);

            Optional<User> result = userRepository.findByEmail(EMAIL);

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo(EMAIL);
            assertThat(result.get().getName()).isEqualTo(NAME);
        }

        @Test
        @DisplayName("should return empty when email does not exist")
        void shouldReturnEmptyWhenNotFound() {
            Optional<User> result = userRepository.findByEmail("nobody@nowhere.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should find user regardless of input email case")
        void shouldFindByEmailCaseNormalized() {
            // User.create normalises email to lower-case on creation
            persistUser("alice@brekfood.com", "Alice", Role.CUSTOMER);

            // Query with exact stored lower-case email must succeed
            assertThat(userRepository.findByEmail("alice@brekfood.com")).isPresent();
        }
    }

    // =========================================================================
    // existsByEmail()
    // =========================================================================

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmail {

        @Test
        @DisplayName("should return true when email exists")
        void shouldReturnTrueWhenExists() {
            persistUser(EMAIL, NAME, ROLE);

            assertThat(userRepository.existsByEmail(EMAIL)).isTrue();
        }

        @Test
        @DisplayName("should return false when email does not exist")
        void shouldReturnFalseWhenNotExists() {
            assertThat(userRepository.existsByEmail("ghost@brekfood.com")).isFalse();
        }
    }

    // =========================================================================
    // Multiple users
    // =========================================================================

    @Nested
    @DisplayName("Multiple users")
    class MultipleUsers {

        @Test
        @DisplayName("should store multiple users with distinct emails")
        void shouldStoreMultipleUsers() {
            persistUser("u1@brekfood.com", "User One", Role.CUSTOMER);
            persistUser("u2@brekfood.com", "User Two", Role.DRIVER);
            persistUser("u3@brekfood.com", "User Three", Role.RESTAURANT_OWNER);

            assertThat(userRepository.existsByEmail("u1@brekfood.com")).isTrue();
            assertThat(userRepository.existsByEmail("u2@brekfood.com")).isTrue();
            assertThat(userRepository.existsByEmail("u3@brekfood.com")).isTrue();
        }
    }
}

