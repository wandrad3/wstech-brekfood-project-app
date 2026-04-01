package com.br.wstech.brekfood.identity.domain.model;

/**
 * Defines all supported roles within the BrekFood platform.
 *
 * <p>Each role controls access to specific bounded contexts and endpoints:
 * <ul>
 *   <li>{@link #CUSTOMER}          — places orders, manages their profile</li>
 *   <li>{@link #RESTAURANT_OWNER}  — manages restaurant, menu items, and accepts orders</li>
 *   <li>{@link #DRIVER}            — picks up and delivers orders, tracks earnings</li>
 *   <li>{@link #ADMIN}             — full platform access, configuration, monitoring</li>
 * </ul>
 *
 * <p>Stored as a {@code VARCHAR} in the database via {@code @Enumerated(EnumType.STRING)}.
 */
public enum Role {

    CUSTOMER("Customer"),
    RESTAURANT_OWNER("Restaurant Owner"),
    DRIVER("Driver"),
    ADMIN("Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the human-readable label for this role.
     * Used in API responses, email templates, and UI display.
     */
    public String getDisplayName() {
        return displayName;
    }
}

