package com.br.wstech.brekfood.identity.domain.repository;
import com.br.wstech.brekfood.identity.domain.model.User;
import java.util.Optional;
import java.util.UUID;
/**
 * Domain repository contract for the User aggregate root.
 *
 * Pure domain interface - no Spring Data or JPA dependencies.
 * Infrastructure implementation: JpaUserRepository (Phase 1.3).
 */
public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}