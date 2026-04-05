package com.br.wstech.brekfood.identity.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Role")
class Role_Test {

    @Nested
    @DisplayName("enum completeness")
    class EnumCompleteness {

        @Test
        @DisplayName("should define exactly 4 roles")
        void shouldDefineExactlyFourRoles() {
            assertThat(Role.values()).hasSize(4);
        }

        @Test
        @DisplayName("should contain CUSTOMER, RESTAURANT_OWNER, DRIVER and ADMIN")
        void shouldContainAllExpectedRoles() {
            assertThat(Role.values())
                    .containsExactlyInAnyOrder(
                            Role.CUSTOMER,
                            Role.RESTAURANT_OWNER,
                            Role.DRIVER,
                            Role.ADMIN
                    );
        }
    }

    @Nested
    @DisplayName("display names")
    class DisplayNames {

        @Test
        @DisplayName("CUSTOMER should have display name 'Customer'")
        void customerDisplayName() {
            assertThat(Role.CUSTOMER.getDisplayName()).isEqualTo("Customer");
        }

        @Test
        @DisplayName("RESTAURANT_OWNER should have display name 'Restaurant Owner'")
        void restaurantOwnerDisplayName() {
            assertThat(Role.RESTAURANT_OWNER.getDisplayName()).isEqualTo("Restaurant Owner");
        }

        @Test
        @DisplayName("DRIVER should have display name 'Driver'")
        void driverDisplayName() {
            assertThat(Role.DRIVER.getDisplayName()).isEqualTo("Driver");
        }

        @Test
        @DisplayName("ADMIN should have display name 'Admin'")
        void adminDisplayName() {
            assertThat(Role.ADMIN.getDisplayName()).isEqualTo("Admin");
        }
    }

    @Nested
    @DisplayName("enum behaviour")
    class EnumBehaviour {

        @Test
        @DisplayName("should resolve role by name via valueOf")
        void shouldResolveByName() {
            assertThat(Role.valueOf("CUSTOMER")).isEqualTo(Role.CUSTOMER);
            assertThat(Role.valueOf("RESTAURANT_OWNER")).isEqualTo(Role.RESTAURANT_OWNER);
            assertThat(Role.valueOf("DRIVER")).isEqualTo(Role.DRIVER);
            assertThat(Role.valueOf("ADMIN")).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("name() should return the enum constant string used in DB")
        void nameShouldMatchDatabaseValue() {
            assertThat(Role.CUSTOMER.name()).isEqualTo("CUSTOMER");
            assertThat(Role.RESTAURANT_OWNER.name()).isEqualTo("RESTAURANT_OWNER");
            assertThat(Role.DRIVER.name()).isEqualTo("DRIVER");
            assertThat(Role.ADMIN.name()).isEqualTo("ADMIN");
        }
    }
}

