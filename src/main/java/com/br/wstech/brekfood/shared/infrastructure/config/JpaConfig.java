package com.br.wstech.brekfood.shared.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA configuration for the BrekFood application.
 *
 * <p>Enables JPA Auditing so that {@code @CreatedDate} and {@code @LastModifiedDate}
 * annotations on {@link com.br.wstech.brekfood.shared.domain.model.BaseEntity} are
 * automatically populated by Spring Data.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // JPA Auditing is enabled via @EnableJpaAuditing.
    // Additional JPA customizations (e.g., NamingStrategy, EntityManagerFactory) go here.
}

