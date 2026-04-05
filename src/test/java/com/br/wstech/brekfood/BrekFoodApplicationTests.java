package com.br.wstech.brekfood;

import com.br.wstech.brekfood.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifies that the Spring application context loads successfully.
 *
 * <p>This is the most fundamental test in the suite. If this fails,
 * no other test will be meaningful.
 *
 * <p>{@code UserRepository} is mocked here because the JPA implementation
 * ({@code JpaUserRepository}) is created in Phase 1.3. The mock is enough
 * to satisfy the dependency graph and boot the context.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("BrekFood Application Context")
class BrekFoodApplicationTests {

    @MockBean
    UserRepository userRepository;

    @Test
    @DisplayName("should load application context without errors")
    void contextLoads() {
        // If the context fails to start, this test will fail automatically.
        // No explicit assertions needed.
    }
}

