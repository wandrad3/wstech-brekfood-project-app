package com.br.wstech.brekfood.identity.infrastructure.persistence;

import com.br.wstech.brekfood.identity.domain.model.User;
import com.br.wstech.brekfood.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter that implements the {@link UserRepository} domain port.
 *
 * <p>This is the only class in the identity infrastructure layer that is
 * visible to the application layer.  It adapts Spring Data JPA
 * ({@link SpringDataUserJpaRepository}) to the pure-domain interface,
 * keeping all JPA/Spring Data details inside the infrastructure boundary.
 *
 * <p><strong>Hexagonal architecture mapping:</strong>
 * <pre>
 *   Application layer ──► UserRepository (port)
 *                                  ▲
 *                    JpaUserRepository (adapter)  ← you are here
 *                                  │
 *                    SpringDataUserJpaRepository  (Spring Data, package-private)
 * </pre>
 *
 * <p>All queries are delegated directly to {@link SpringDataUserJpaRepository}.
 * No business logic must ever be placed here.
 */
@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserJpaRepository springDataRepo;

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link SpringDataUserJpaRepository#save(Object)}.
     * JPA merge semantics apply: returns the managed entity (may differ from the argument).
     */
    @Override
    public User save(User user) {
        return springDataRepo.save(user);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link SpringDataUserJpaRepository#findById(Object)}.
     */
    @Override
    public Optional<User> findById(UUID id) {
        return springDataRepo.findById(id);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link SpringDataUserJpaRepository#findByEmail(String)}.
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return springDataRepo.findByEmail(email);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link SpringDataUserJpaRepository#existsByEmail(String)}.
     */
    @Override
    public boolean existsByEmail(String email) {
        return springDataRepo.existsByEmail(email);
    }
}

