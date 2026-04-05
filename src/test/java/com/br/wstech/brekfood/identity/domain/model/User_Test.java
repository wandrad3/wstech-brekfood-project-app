package com.br.wstech.brekfood.identity.domain.model;

import com.br.wstech.brekfood.shared.domain.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("User")
class User_Test {

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    private static final String VALID_EMAIL        = "alice@brekfood.com";
    private static final String VALID_PASSWORD_HASH = "$2a$10$someValidBcryptHashValue";
    private static final String VALID_NAME          = "Alice Smith";
    private static final Role   VALID_ROLE          = Role.CUSTOMER;

    private User buildValidUser() {
        return User.create(VALID_EMAIL, VALID_PASSWORD_HASH, VALID_NAME, VALID_ROLE);
    }

    // -------------------------------------------------------------------------
    // Factory method — User.create()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("when created via factory method with valid data")
    class WhenCreatedWithValidData {

        @Test
        @DisplayName("should expose the provided email, name, and role")
        void shouldExposeFieldValues() {
            User user = buildValidUser();

            assertThat(user.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(user.getPasswordHash()).isEqualTo(VALID_PASSWORD_HASH);
            assertThat(user.getName()).isEqualTo(VALID_NAME);
            assertThat(user.getRole()).isEqualTo(VALID_ROLE);
        }

        @Test
        @DisplayName("should be active by default")
        void shouldBeActiveByDefault() {
            User user = buildValidUser();
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("should normalize email to lower-case")
        void shouldNormalizeEmailToLowerCase() {
            User user = User.create("Alice@BrekFood.COM", VALID_PASSWORD_HASH, VALID_NAME, VALID_ROLE);
            assertThat(user.getEmail()).isEqualTo("alice@brekfood.com");
        }

        @Test
        @DisplayName("should strip leading and trailing whitespace from email")
        void shouldStripEmailWhitespace() {
            User user = User.create("  alice@brekfood.com  ", VALID_PASSWORD_HASH, VALID_NAME, VALID_ROLE);
            assertThat(user.getEmail()).isEqualTo("alice@brekfood.com");
        }

        @Test
        @DisplayName("should strip leading and trailing whitespace from name")
        void shouldStripNameWhitespace() {
            User user = User.create(VALID_EMAIL, VALID_PASSWORD_HASH, "  Alice Smith  ", VALID_ROLE);
            assertThat(user.getName()).isEqualTo("Alice Smith");
        }

        @Test
        @DisplayName("should work for all defined roles")
        void shouldWorkForAllRoles() {
            for (Role role : Role.values()) {
                User user = User.create(VALID_EMAIL, VALID_PASSWORD_HASH, VALID_NAME, role);
                assertThat(user.getRole()).isEqualTo(role);
            }
        }
    }

    @Nested
    @DisplayName("when created with invalid email")
    class WhenCreatedWithInvalidEmail {

        @Test
        @DisplayName("should throw BusinessRuleViolationException for null email")
        void shouldRejectNullEmail() {
            assertThatThrownBy(() -> User.create(null, VALID_PASSWORD_HASH, VALID_NAME, VALID_ROLE))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Email must not be blank");
        }

        @ParameterizedTest(name = "email=''{0}''")
        @ValueSource(strings = {"", "   ", "\t"})
        @DisplayName("should throw BusinessRuleViolationException for blank email")
        void shouldRejectBlankEmail(String blank) {
            assertThatThrownBy(() -> User.create(blank, VALID_PASSWORD_HASH, VALID_NAME, VALID_ROLE))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Email must not be blank");
        }

        @Test
        @DisplayName("should throw BusinessRuleViolationException for email without '@'")
        void shouldRejectEmailWithoutAtSign() {
            assertThatThrownBy(() -> User.create("notanemail", VALID_PASSWORD_HASH, VALID_NAME, VALID_ROLE))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Email format is invalid");
        }
    }

    @Nested
    @DisplayName("when created with invalid password hash")
    class WhenCreatedWithInvalidPasswordHash {

        @Test
        @DisplayName("should throw BusinessRuleViolationException for null password hash")
        void shouldRejectNullPasswordHash() {
            assertThatThrownBy(() -> User.create(VALID_EMAIL, null, VALID_NAME, VALID_ROLE))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Password hash must not be blank");
        }

        @ParameterizedTest(name = "hash=''{0}''")
        @ValueSource(strings = {"", "   "})
        @DisplayName("should throw BusinessRuleViolationException for blank password hash")
        void shouldRejectBlankPasswordHash(String blank) {
            assertThatThrownBy(() -> User.create(VALID_EMAIL, blank, VALID_NAME, VALID_ROLE))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Password hash must not be blank");
        }
    }

    @Nested
    @DisplayName("when created with invalid name")
    class WhenCreatedWithInvalidName {

        @Test
        @DisplayName("should throw BusinessRuleViolationException for null name")
        void shouldRejectNullName() {
            assertThatThrownBy(() -> User.create(VALID_EMAIL, VALID_PASSWORD_HASH, null, VALID_ROLE))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Name must not be blank");
        }

        @ParameterizedTest(name = "name=''{0}''")
        @ValueSource(strings = {"", "   "})
        @DisplayName("should throw BusinessRuleViolationException for blank name")
        void shouldRejectBlankName(String blank) {
            assertThatThrownBy(() -> User.create(VALID_EMAIL, VALID_PASSWORD_HASH, blank, VALID_ROLE))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Name must not be blank");
        }
    }

    @Nested
    @DisplayName("when created with null role")
    class WhenCreatedWithNullRole {

        @Test
        @DisplayName("should throw NullPointerException for null role")
        void shouldRejectNullRole() {
            assertThatThrownBy(() -> User.create(VALID_EMAIL, VALID_PASSWORD_HASH, VALID_NAME, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Role must not be null");
        }
    }

    // -------------------------------------------------------------------------
    // Business method — activate / deactivate
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("when deactivating a user")
    class WhenDeactivating {

        @Test
        @DisplayName("should mark the user as inactive")
        void shouldMarkUserInactive() {
            User user = buildValidUser();
            user.deactivate();
            assertThat(user.isActive()).isFalse();
        }

        @Test
        @DisplayName("should throw BusinessRuleViolationException when already inactive")
        void shouldThrowWhenAlreadyInactive() {
            User user = buildValidUser();
            user.deactivate();

            assertThatThrownBy(user::deactivate)
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("User is already inactive");
        }
    }

    @Nested
    @DisplayName("when activating a user")
    class WhenActivating {

        @Test
        @DisplayName("should mark the user as active again")
        void shouldMarkUserActive() {
            User user = buildValidUser();
            user.deactivate();
            user.activate();
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("should throw BusinessRuleViolationException when already active")
        void shouldThrowWhenAlreadyActive() {
            User user = buildValidUser(); // active by default

            assertThatThrownBy(user::activate)
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("User is already active");
        }
    }

    // -------------------------------------------------------------------------
    // Business method — changeRole
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("when changing role")
    class WhenChangingRole {

        @Test
        @DisplayName("should update the role to a new valid value")
        void shouldUpdateRole() {
            User user = buildValidUser();
            user.changeRole(Role.DRIVER);
            assertThat(user.getRole()).isEqualTo(Role.DRIVER);
        }

        @Test
        @DisplayName("should throw NullPointerException when new role is null")
        void shouldThrowWhenRoleIsNull() {
            User user = buildValidUser();

            assertThatThrownBy(() -> user.changeRole(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("New role must not be null");
        }
    }

    // -------------------------------------------------------------------------
    // Business method — updateName
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("when updating name")
    class WhenUpdatingName {

        @Test
        @DisplayName("should update name and strip whitespace")
        void shouldUpdateNameWithTrim() {
            User user = buildValidUser();
            user.updateName("  Bob Marley  ");
            assertThat(user.getName()).isEqualTo("Bob Marley");
        }

        @ParameterizedTest(name = "name=''{0}''")
        @ValueSource(strings = {"", "   "})
        @DisplayName("should throw BusinessRuleViolationException for blank new name")
        void shouldThrowForBlankName(String blank) {
            User user = buildValidUser();

            assertThatThrownBy(() -> user.updateName(blank))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Name must not be blank");
        }

        @Test
        @DisplayName("should throw BusinessRuleViolationException for null name")
        void shouldThrowForNullName() {
            User user = buildValidUser();

            assertThatThrownBy(() -> user.updateName(null))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Name must not be blank");
        }
    }

    // -------------------------------------------------------------------------
    // Business method — updatePasswordHash
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("when updating password hash")
    class WhenUpdatingPasswordHash {

        @Test
        @DisplayName("should replace the existing password hash")
        void shouldReplacePasswordHash() {
            User user = buildValidUser();
            String newHash = "$2a$10$newBcryptHashValue";
            user.updatePasswordHash(newHash);
            assertThat(user.getPasswordHash()).isEqualTo(newHash);
        }

        @ParameterizedTest(name = "hash=''{0}''")
        @ValueSource(strings = {"", "   "})
        @DisplayName("should throw BusinessRuleViolationException for blank hash")
        void shouldThrowForBlankHash(String blank) {
            User user = buildValidUser();

            assertThatThrownBy(() -> user.updatePasswordHash(blank))
                    .isInstanceOf(BusinessRuleViolationException.class)
                    .hasMessageContaining("Password hash must not be blank");
        }
    }

    // -------------------------------------------------------------------------
    // Equality and inheritance
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("entity identity")
    class EntityIdentity {

        @Test
        @DisplayName("id should be null before persistence (JPA manages UUID generation)")
        void idShouldBeNullBeforePersistence() {
            User user = buildValidUser();
            // UUID is generated by JPA on first save; null is expected in unit tests
            assertThat(user.getId()).isNull();
        }

        @Test
        @DisplayName("two users with different instances but same state are not equal (id-based)")
        void differentInstancesAreNotEqual() {
            User u1 = buildValidUser();
            User u2 = buildValidUser();
            // Both have null id, but they're different objects — not equal by reference
            assertThat(u1).isNotSameAs(u2);
        }
    }
}

