package com.br.wstech.brekfood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * BrekFood — Fairness-first delivery platform.
 * ...existing code...
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BrekFoodApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrekFoodApplication.class, args);
    }
}

