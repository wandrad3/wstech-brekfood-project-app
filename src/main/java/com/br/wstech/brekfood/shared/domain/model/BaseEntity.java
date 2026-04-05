package com.br.wstech.brekfood.shared.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.UUID;

/**
 * Abstract base entity for all BrekFood domain entities.
 *
 * <p>Provides:
 * <ul>
 *   <li>UUID primary key with auto-generation</li>
 *   <li>Immutable creation timestamp</li>
 *   <li>Mutable last-updated timestamp</li>
 *   <li>Equality based on ID only (JPA-safe)</li>
 * </ul>
 *
 * <p>Concrete entities must annotate their class with {@code @Entity} and
 * {@code @EntityListeners(AuditingEntityListener.class)} to activate JPA auditing.
 */
@MappedSuperclass
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

