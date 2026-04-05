package com.br.wstech.brekfood.identity.domain.model;

import com.br.wstech.brekfood.shared.domain.exception.BusinessRuleViolationException;
import com.br.wstech.brekfood.shared.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Locale;
import java.util.Objects;

/**
 * Aggregate root for the Identity bounded context.
 *
 * <p>Represents a registered platform user regardless of role.
 * Identity is always enforced by the {@link #id} field inherited from {@link BaseEntity}.
 *
 * <p><strong>Invariants (always true after construction):</strong>
 * <ul>
 *   <li>Email is non-blank, normalized to lower-case, and contains '@'</li>
 *   <li>Password hash is non-blank (BCrypt managed by the application layer)</li>
 *   <li>Name is non-blank</li>
 *   <li>Role is non-null</li>
 *   <li>A newly created user is always active</li>
 * </ul>
 *
 * <p><strong>Usage — creation via named factory method:</strong>
 * <pre>
 *   User user = User.create("Alice@Example.com", bcryptHash, "Alice", Role.CUSTOMER);
 * </pre>
 *
 * <p>Direct field mutation is not allowed (no public setters).
 * Use the provided business methods to change state.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uc_users_email", columnNames = "email")
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@ToString(exclude = "passwordHash")  // never log password hash
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** BCrypt hash of the user's password. Never store plain-text passwords. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(nullable = false)
    private boolean active;

    // -------------------------------------------------------------------------
    // Constructor (private — only callable by the factory method)
    // -------------------------------------------------------------------------

    private User(String email, String passwordHash, String name, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.active = true;
    }

    // -------------------------------------------------------------------------
    // Named factory method — the only public way to create a new User
    // -------------------------------------------------------------------------

    /**
     * Creates a new active {@link User} with all invariants validated.
     *
     * <p>Email is normalized: lower-cased and stripped of leading/trailing whitespace.
     * Name is stripped of leading/trailing whitespace.
     *
     * @param email        the user's email address (must contain '@')
     * @param passwordHash BCrypt hash of the plain-text password (never raw password)
     * @param name         the user's display name
     * @param role         the initial platform role
     * @return a new active {@link User} instance
     * @throws BusinessRuleViolationException if any argument is blank or invalid
     * @throws NullPointerException           if {@code role} is null
     */
    public static User create(String email, String passwordHash, String name, Role role) {
        requireNonBlank(email, "Email must not be blank");
        requireValidEmailFormat(email);
        requireNonBlank(passwordHash, "Password hash must not be blank");
        requireNonBlank(name, "Name must not be blank");
        Objects.requireNonNull(role, "Role must not be null");

        return new User(
                email.toLowerCase(Locale.ROOT).strip(),
                passwordHash,
                name.strip(),
                role
        );
    }

    // -------------------------------------------------------------------------
    // Business methods — explicit intent, guard all invariants
    // -------------------------------------------------------------------------

    /**
     * Activates this user account.
     *
     * @throws BusinessRuleViolationException if the user is already active
     */
    public void activate() {
        if (this.active) {
            throw new BusinessRuleViolationException("User is already active");
        }
        this.active = true;
    }

    /**
     * Deactivates this user account (soft delete / ban).
     *
     * @throws BusinessRuleViolationException if the user is already inactive
     */
    public void deactivate() {
        if (!this.active) {
            throw new BusinessRuleViolationException("User is already inactive");
        }
        this.active = false;
    }

    /**
     * Changes the user's platform role.
     *
     * @param newRole the new role to assign (must not be null)
     * @throws NullPointerException if {@code newRole} is null
     */
    public void changeRole(Role newRole) {
        Objects.requireNonNull(newRole, "New role must not be null");
        this.role = newRole;
    }

    /**
     * Updates the user's display name.
     *
     * @param newName the new display name (must not be blank)
     * @throws BusinessRuleViolationException if the new name is blank
     */
    public void updateName(String newName) {
        requireNonBlank(newName, "Name must not be blank");
        this.name = newName.strip();
    }

    /**
     * Replaces the stored password hash (called after a successful password change).
     *
     * @param newPasswordHash BCrypt hash of the new password (must not be blank)
     * @throws BusinessRuleViolationException if the new hash is blank
     */
    public void updatePasswordHash(String newPasswordHash) {
        requireNonBlank(newPasswordHash, "Password hash must not be blank");
        this.passwordHash = newPasswordHash;
    }

    // -------------------------------------------------------------------------
    // Internal validation helpers
    // -------------------------------------------------------------------------

    private static void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException(message);
        }
    }

    private static void requireValidEmailFormat(String email) {
        if (!email.contains("@")) {
            throw new BusinessRuleViolationException("Email format is invalid: must contain '@'");
        }
    }
}

