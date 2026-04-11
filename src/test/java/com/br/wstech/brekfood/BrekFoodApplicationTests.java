package com.br.wstech.brekfood;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifies that the Spring application context loads successfully.
 *
 * <p>This is the most fundamental test in the suite. If this fails,
 * no other test will be meaningful.
 *
 * <p>As of Phase 1.3, {@code JpaUserRepository} is fully implemented and
 * satisfies the {@code UserRepository} domain port — no mock is needed.
 * The test profile uses H2 in-memory with Hibernate {@code create-drop}
 * so no PostgreSQL instance is required.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("BrekFood Application Context")
class BrekFoodApplicationTests {


    @Test
    @DisplayName("should load application context without errors")
    void contextLoads() {
        // If the context fails to start, this test will fail automatically.
        // No explicit assertions needed.
    }
}

