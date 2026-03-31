package com.br.wstech.brekfood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BrekFood — Fairness-first delivery platform.
 *
 * <p>Modular monolith organized by DDD bounded contexts:
 * <ul>
 *   <li>identity   — authentication, authorization, user management</li>
 *   <li>customer   — customer profiles and preferences</li>
 *   <li>restaurant — restaurant and menu management</li>
 *   <li>order      — order lifecycle and state machine</li>
 *   <li>delivery   — driver management and dispatch algorithm</li>
 *   <li>pricing    — fee calculation and surge pricing engine</li>
 *   <li>payment    — payment processing and webhooks</li>
 *   <li>earnings   — driver earnings and fairness engine</li>
 * </ul>
 */
@SpringBootApplication
public class BrekFoodApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrekFoodApplication.class, args);
    }
}

