package com.br.wstech.brekfood.identity.infrastructure.persistence;

import com.br.wstech.brekfood.identity.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the {@link User} entity.
 *
 * <p><strong>Visibility: package-private</strong> — this interface must NOT be accessed
 * directly by the application or domain layers.  All access must go through
 * {@link JpaUserRepository}, which adapts this interface to the domain port.
 *
 * <p>Naming convention:
 * <pre>
 *   Domain port      : UserRepository                (identity/domain/repository)
 *   Infrastructure   : JpaUserRepository             (identity/infrastructure/persistence) ← public adapter
 *   Spring Data      : SpringDataUserJpaRepository   (identity/infrastructure/persistence) ← package-private
 * </pre>
 */
interface SpringDataUserJpaRepository extends JpaRepository<User, UUID> {

    /**
     * Looks up a user by their (normalized, lower-cased) e-mail.
     *
     * @param email the e-mail address to search for
     * @return an {@link Optional} containing the matching user, or empty if none found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given e-mail already exists.
     *
     * @param email the e-mail address to check
     * @return {@code true} if at least one user with this e-mail exists
     */
    boolean existsByEmail(String email);
}

