package com.br.wstech.brekfood.shared.interfaces.rest;

import com.br.wstech.brekfood.shared.interfaces.rest.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiResponse")
class ApiResponse_Test {

    @Test
    @DisplayName("should wrap data in response envelope")
    void shouldWrapData() {
        String payload = "brekfood-data";

        ApiResponse<String> response = ApiResponse.of(payload);

        assertThat(response.getData()).isEqualTo(payload);
    }

    @Test
    @DisplayName("should set a non-null timestamp on creation")
    void shouldSetTimestamp() {
        Instant before = Instant.now();

        ApiResponse<String> response = ApiResponse.of("data");

        Instant after = Instant.now();
        assertThat(response.getTimestamp())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("should accept null data")
    void shouldAcceptNullData() {
        ApiResponse<Object> response = ApiResponse.of(null);

        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("should work with any type (generic)")
    void shouldWorkWithAnyType() {
        record OrderSummary(String id, String status) {}
        OrderSummary summary = new OrderSummary("ord-1", "PENDING");

        ApiResponse<OrderSummary> response = ApiResponse.of(summary);

        assertThat(response.getData().id()).isEqualTo("ord-1");
        assertThat(response.getData().status()).isEqualTo("PENDING");
    }
}

