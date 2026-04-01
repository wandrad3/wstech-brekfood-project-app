# BrekFood — Project Plan & Deep Specification

> **Phase 0 Status: ✅ COMPLETE (2026-04-01)**
> 23 unit tests passing | PostgreSQL + Docker infrastructure ready | JaCoCo coverage thresholds configured
> **Phase 1.1 Status: ✅ COMPLETE (2026-04-01)**
> 70 unit tests passing | User aggregate root, Role enum, UserRepository (domain port), UserNotFoundException
> **Next: Phase 1.2 — Application Layer (RegisterCommand, AuthService, JwtTokenProvider)**

---

## 1. Vision

BrekFood is a fairness-first, transparent, and scalable delivery platform evolving into a super app. It prioritizes:

* Fair earnings for drivers
* Lower, transparent fees for restaurants
* Fast, modern UX for customers

---

## 2. Deep Domain Modeling (DDD)

### 2.1 Bounded Contexts

* Identity
* Customer
* Restaurant
* Order
* Delivery
* Payment
* Pricing
* Earnings

---

## 2.2 Aggregates

### Order Aggregate

* Root: Order
* Entities: OrderItem
* Value Objects: Money, Address, OrderStatus

### Delivery Aggregate

* Root: Delivery
* Entities: DriverAssignment
* Value Objects: Location, Distance

### Restaurant Aggregate

* Root: Restaurant
* Entities: MenuItem
* Value Objects: Price, Availability

### Driver Aggregate

* Root: Driver
* Entities: DriverStatus
* Value Objects: EarningsSnapshot

---

## 2.3 Entities

* Order
* OrderItem
* Restaurant
* MenuItem
* Driver
* Delivery

---

## 2.4 Value Objects

* Money (amount, currency)
* Distance (km)
* TimeWindow
* Location (lat, lng)
* FeeBreakdown
* EarningsBreakdown

---

## 2.5 Domain Events

* OrderCreated
* OrderAccepted
* OrderPrepared
* OrderReady
* OrderAssignedToDriver
* DeliveryStarted
* DeliveryCompleted
* PaymentProcessed

---

## 3. OpenAPI (v1)

### Base Path

/api/v1

---

### Auth

POST /auth/register
POST /auth/login

---

### Customer

GET /customers/me
PUT /customers/me

---

### Restaurant

POST /restaurants
GET /restaurants
GET /restaurants/{id}

---

### Order

POST /orders
GET /orders/{id}
PATCH /orders/{id}/status

---

### Delivery

POST /deliveries/assign
PATCH /deliveries/{id}/status

---

### Payment

POST /payments
POST /payments/webhook

---

## Schemas (v1)

### Order

{
id,
customerId,
restaurantId,
items,
totalAmount,
status
}

---

## 4. Pricing Engine (CORE DIFFERENTIATOR)

### Formula

platform_fee = base_rate * order_value

where:

* base_rate ∈ [0.05, 0.15]

---

### Advanced Formula

platform_fee = (base_rate * order_value)
+ surge_multiplier
- loyalty_discount

---

### Fee Breakdown

* Restaurant fee
* Delivery fee
* Platform fee

---

## 5. Earnings Engine (CORE DIFFERENTIATOR)

### Formula

driver_earnings = base_fare
+ (distance_km * per_km_rate)
+ (time_min * per_min_rate)
+ bonus

---

### Fairness Rules

* Minimum guaranteed earnings per hour
* No driver starvation (max idle time)
* Balanced distribution

---

### Example Simulation

Order: R$50
Distance: 5km

Driver earns:
= 5 (base)

* 5*2 (distance)
  = R$15

---

## 6. Dispatch Algorithm (CRITICAL)

### Objectives

* Fair distribution
* Minimize delivery time
* Maximize driver earnings stability

---

### Scoring Function

score = w1 * distance_score
+ w2 * idle_time_score
+ w3 * earnings_balance_score

---

### Rules

* Prefer nearest drivers
* Boost drivers with low earnings
* Penalize over-assigned drivers

---

### Anti-Concentration

* Cap max orders per driver window
* Rotate assignments

---

## 7. Spring Boot Multi-Module Structure

```
brekfood-backend/
  build.gradle

  identity-service/
  order-service/
  delivery-service/
  payment-service/
  pricing-engine/
  earnings-engine/

  shared-kernel/
    domain/
    utils/
```

---

## 7.1 Module Structure Example

```
order-service/
  domain/
  application/
  infrastructure/
  interfaces/
```

---

## 8. Evolution Path

1. Modular monolith
2. Extract delivery + payment
3. Event-driven architecture

---

## 9. Final Insight

BrekFood is not just delivery:

It is:

* A pricing system
* An earnings system
* A fairness engine

That is your true competitive advantage.

---

## 10. Advanced Simulation Engine (Market-Grade)

This section defines a probabilistic, adaptive simulator to validate BrekFood's fairness, pricing, and dispatch under realistic conditions.

---

### 10.1 Simulation Goals

* Validate fairness vs efficiency trade-offs
* Stress-test dispatch under peak demand
* Measure driver retention indicators
* Optimize pricing and earnings dynamically

---

### 10.2 Simulation Model

#### Time Model

* Discrete time steps (Δt = 1 minute)
* Simulation window: 24h cycles

#### Entities

* Drivers (N)
* Orders (Poisson arrival)
* مناطق (zones/clusters)

---

### 10.3 Demand Generation (Stochastic)

Orders follow Poisson process:

λ(t) varies by time of day:

* Lunch peak (11h–14h): λ = high
* Dinner peak (18h–22h): λ = very high
* Off-peak: λ = low

Order attributes sampled from distributions:

* value ~ Uniform(20, 120)
* distance ~ Normal(μ=4km, σ=1.5)

---

### 10.4 Driver Behavior Model

Each driver has state:

* location
* status (idle, assigned, delivering)
* fatigue factor
* acceptance probability

Acceptance probability:

P(accept) = sigmoid(earnings_expectation - effort)

---

### 10.5 Dispatch Algorithm v2 (Adaptive)

#### Base Score

score = w1 * proximity
+ w2 * idle_time
+ w3 * earnings_gap
+ w4 * acceptance_probability

---

#### Dynamic Weights

Weights adapt in runtime:

* If inequality ↑ → increase w3
* If delivery delay ↑ → increase w1

---

### 10.6 Geographic Clustering

City divided into zones:

* Grid or hex-based clustering

Rules:

* Drivers prefer local zone
* Cross-zone dispatch penalized

---

### 10.7 Order Batching (Advanced)

If:

* Same route
* Close pickup points

Then:

* Combine orders

Constraint:

* Max delay threshold

---

### 10.8 Earnings Engine v2 (Dynamic)

Base:

E = base + (d * r_km) + (t * r_min)

---

Dynamic Adjustments:

E = E * demand_multiplier * fairness_multiplier

Where:

fairness_multiplier = 1 + (avg_earnings - driver_earnings)/avg_earnings

---

### 10.9 Surge Pricing Model

surge = f(demand / supply)

Example:

if demand/supply > 1.5 → surge = 1.3
if demand/supply > 2.0 → surge = 1.6

---

### 10.10 Learning System (Adaptive)

System updates parameters based on metrics:

* Reinforcement-like adjustment
* Reward function:

  * minimize delivery time
  * minimize earnings variance

---

### 10.11 Metrics Collected

#### Efficiency

* Avg delivery time
* Order completion rate

#### Fairness

* Earnings variance
* Gini coefficient (drivers)

#### Stability

* Driver churn risk (proxy)
* Idle time distribution

---

### 10.12 Simulation Scenarios

#### Scenario A — High Demand

* Stress dispatch
* Evaluate surge

#### Scenario B — Driver Scarcity

* Validate incentives

#### Scenario C — Fairness Stress

* Uneven earnings start
* Observe convergence

---

### 10.13 Expected Outcomes

With adaptive system:

* Earnings converge over time
* Delivery time slightly increases (~5–10%)
* Driver retention improves significantly

---

### 10.14 Key Insight

A static system loses to market dynamics.

A dynamic, adaptive system:

* Learns demand patterns
* Balances fairness vs efficiency
* Becomes stronger over time

This is the foundation of a competitive BrekFood platform.

---

## 11. Phase 0 Completion Summary

### Delivered (2026-04-01)

| Area                  | Artifact                              | Detail                                               |
|-----------------------|---------------------------------------|------------------------------------------------------|
| Project bootstrap     | `pom.xml`, `BrekFoodApplication.java` | brekfood-app, Java 17, Spring Boot 3.5.5             |
| DDD skeleton          | 8 bounded contexts × 4 layers         | domain / application / infrastructure / interfaces   |
| Shared Kernel         | BaseEntity, exceptions, handlers      | UUID entities, 404/422 hierarchy, global error format|
| Dependencies          | Flyway, SpringDoc, MapStruct, JJWT    | All pinned, Lombok–MapStruct binding configured      |
| Configuration         | `application.properties`              | HikariCP pool, env-var-driven, UTC timezone          |
| Dev profile           | `application-dev.properties`          | Verbose logging, Flyway repair, multi-origin CORS    |
| Test profile          | `application-test.properties`         | H2 in-memory, Flyway disabled                        |
| Coverage gate         | JaCoCo `check` goal in `pom.xml`      | LINE ≥ 70%, BRANCH ≥ 60%, CLASS ≥ 80%               |
| Containerization      | `docker-compose.yml`, `Dockerfile`    | Multi-stage layered build, non-root runtime, healthcheck |
| Environment template  | `.env.example`                        | Full variable reference for dev/prod                 |
| Tests (Phase 0)       | 23 unit tests passing                 | Exceptions, API responses, JWT config validation     |

## 12. Phase 1.1 Completion Summary

### Delivered (2026-04-01)

| Artifact                        | Package                                              | Key Decisions                                                         |
|---------------------------------|------------------------------------------------------|-----------------------------------------------------------------------|
| `Role.java`                     | `identity.domain.model`                              | 4 values with `displayName`; stored as VARCHAR via `@Enumerated(STRING)` |
| `User.java`                     | `identity.domain.model`                              | Aggregate root; `User.create()` factory method; no public setters; email normalized on create; `@NoArgsConstructor(PROTECTED)` for JPA only |
| `UserNotFoundException.java`    | `identity.domain.exception`                          | Extends `EntityNotFoundException`; UUID + email constructors          |
| `UserRepository.java`           | `identity.domain.repository`                         | Pure Java interface (domain port); 0 framework imports; JpaUserRepository in Phase 1.3 |
| `Role_Test.java`                | test `identity.domain.model`                         | 8 tests: enum completeness, display names, valueOf/name behaviour     |
| `User_Test.java`                | test `identity.domain.model`                         | 31 tests: factory method, all business methods, parameterized invalid inputs |
| `UserNotFoundException_Test.java` | test `identity.domain.exception`                   | 6 tests: message format, exception hierarchy                          |
| **Tests total**                 | —                                                    | **70 unit tests passing, coverage gate satisfied**                    |

