# BrekFood - Memory & Context Document

> Last updated: 2026-04-05
> Status: **Phase 1.2 Complete — 116 tests passing, Auth application layer + JWT security ready**

---

## 1. Project Identity

| Key              | Value                                      |
|------------------|--------------------------------------------|
| Name             | BrekFood                                   |
| Type             | Fairness-first delivery platform (super app) |
| Organization     | WSTech Solutions                           |
| Repository       | wstech-brekfood-project-app                |
| Artifact ID      | `brekfood-app`                             |
| Base Package     | `com.br.wstech.brekfood`                   |
| Main Class       | `BrekFoodApplication`                      |
| Language         | Java 17                                    |
| Framework        | Spring Boot 3.5.5                          |
| Build Tool       | Maven (mvnw)                               |
| Database         | PostgreSQL 15                              |
| Test DB          | H2 in-memory (profile: `test`)             |
| Test Coverage    | JaCoCo 0.8.12                              |

---

## 2. Current State (Snapshot)

### What EXISTS today (Phase 1.2 complete):
- `BrekFoodApplication.java` — main class com `@ConfigurationPropertiesScan`
- Full DDD package skeleton: **8 bounded contexts × 4 layers**
- **Shared Kernel** completo:
  - `BaseEntity` — abstract UUID entity com JPA auditing
  - `DomainException` hierarchy — `EntityNotFoundException` (404), `BusinessRuleViolationException` (422)
  - `ApiResponse<T>` / `ApiError` + `FieldError` — envelopes padronizados
  - `GlobalExceptionHandler` — mapeia 404/401/422/400/500, sem vazamento de detalhes internos
  - `JpaConfig` — `@EnableJpaAuditing`
  - `WebConfig` — CORS via `brekfood.cors.allowed-origins`
  - `OpenApiConfig` — SpringDoc com JWT Bearer scheme e 8 tags (uma por bounded context)
  - `JwtProperties` — `@ConfigurationProperties` record validado com Bean Validation
  - `SecurityConfig` — BCrypt bean, stateless `SecurityFilterChain`, auth endpoints públicos
- **Flyway** configurado: `db/migration/V0__baseline.sql` criado
- **Testcontainers**: `AbstractIntegrationTest` base class com PostgreSQL 15 container
- **Configuration** completa: `application.properties` (HikariCP + env vars), `application-dev.properties`, `application-test.properties` (com JWT test values)
- **JaCoCo** — check goal: LINE ≥ 70%, BRANCH ≥ 60%, CLASS ≥ 80%
- **Infrastructure**: `docker-compose.yml`, `Dockerfile` (multi-stage), `.env.example`
- **Phase 1.1 — Identity Domain Layer:**
  - `Role` enum — CUSTOMER, RESTAURANT_OWNER, DRIVER, ADMIN com `displayName`, armazenado como VARCHAR
  - `User` entity — aggregate root, `User.create()` static factory enforces all invariants
  - `UserNotFoundException` — extends `EntityNotFoundException`
  - `UserRepository` — pure domain interface (port), sem Spring/JPA
- **Phase 1.2 — Identity Application Layer + Security:**
  - `RegisterCommand` / `LoginCommand` — records imutáveis com Jakarta Validation
  - `AuthResponse` — DTO record (token, expiresAt, userId, email, name, roleDisplayName)
  - `TokenProvider` — output port interface (hexagonal arch)
  - `TokenDetails` — record (token, expiresAt)
  - `AuthService` — interface de use case
  - `AuthServiceImpl` — implementação: `existsByEmail → encode → User.create → save → generateToken`
  - `EmailAlreadyRegisteredException` — extends `BusinessRuleViolationException` (422)
  - `InvalidCredentialsException` — extends `DomainException` (401, mensagem vaga, anti-enumeration)
  - `JwtTokenProvider` — implements `TokenProvider`, JJWT 0.12.6, `@PostConstruct` key init
  - `JwtAuthenticationFilter` — `OncePerRequestFilter`, extrai Bearer token, popula `SecurityContextHolder`
- **116 testes unitários passando**

### What DOES NOT exist yet (upcoming phases):
- Phase 1.3: `JpaUserRepository`, `UserDetailsServiceImpl`
- Phase 1.4: `/api/v1/auth/register` e `/api/v1/auth/login` controllers
- Phase 1.5: `V1__create_users_table.sql`
- Phase 1.6: Integration tests for auth flow
- Phases 2-10: Remaining bounded contexts

---

## 3. Architecture Decisions (ADRs)

### ADR-001: Modular Monolith First
- **Decision**: Start as a modular monolith with clear bounded context separation via packages
- **Rationale**: Faster development, simpler deployment, easier refactoring
- **Evolution**: Extract delivery + payment services later, then move to event-driven architecture
- **Package Strategy**: Each bounded context gets its own package subtree with DDD layers

### ADR-002: DDD Layered Architecture per Module
- **Decision**: Each bounded context follows `domain / application / infrastructure / interfaces` layers
- **Rationale**: Clean separation of concerns, testability, domain purity
- **Structure**:
  ```
  context-name/
    domain/        -> Entities, Value Objects, Domain Events, Repository interfaces
    application/   -> Use Cases / Application Services, DTOs, Command/Query handlers
    infrastructure/ -> JPA implementations, external service clients, messaging
    interfaces/    -> REST Controllers, Request/Response models
  ```

### ADR-003: Spring Boot + Maven Single Module (Phase 1)
- **Decision**: Keep single `pom.xml` for Phase 1 (monolith), reorganize packages internally
- **Rationale**: Avoid premature multi-module complexity; package-level separation is sufficient
- **Future**: Migrate to multi-module Maven when extracting services

### ADR-004: PostgreSQL as Primary Datastore
- **Decision**: PostgreSQL for all persistent data
- **Rationale**: Rich JSON support, PostGIS potential for location data, mature ecosystem

### ADR-006: User.create() as the sole public constructor
- **Decision**: The `User` entity has a `private` constructor. All creation goes through `User.create(email, passwordHash, name, role)`.
- **Rationale**: The factory method is the single enforcement point for all domain invariants (non-blank fields, email format, normalized email). It's impossible to create an invalid User.
- **Side effect**: `@NoArgsConstructor(access = PROTECTED)` is preserved for JPA only.

### ADR-007: UserRepository as a pure domain port
- **Decision**: `UserRepository` is a plain Java interface with zero framework imports (no `JpaRepository`, no Spring annotations).
- **Rationale**: Strict DDD hexagonal port — the domain dictates the contract, infrastructure fulfills it.
- **Implementation**: `JpaUserRepository` in `identity/infrastructure/persistence/` (Phase 1.3) extends both `UserRepository` and `JpaRepository<User, UUID>`.

### ADR-009: TokenProvider output port isolates JWT infrastructure
- **Decision**: `AuthServiceImpl` depends only on the `TokenProvider` interface (application layer). `JwtTokenProvider` (JJWT-specific) lives in `infrastructure/security/`.
- **Rationale**: The application layer has zero JJWT or framework imports. Swapping the JWT library requires only a new `TokenProvider` implementation — no service changes.
- **Port**: `identity/application/port/out/TokenProvider` + `TokenDetails` record.

### ADR-010: SecurityConfig in shared/infrastructure/config
- **Decision**: `SecurityConfig` is placed in the shared kernel (`shared/infrastructure/config/`) rather than inside the identity context.
- **Rationale**: Security is a cross-cutting concern. The filter chain governs ALL contexts, not just identity. Placing it in identity would create an upstream dependency on identity from all contexts.
- **Tradeoff**: `SecurityConfig` imports `JwtAuthenticationFilter` from the identity context — this is an accepted dependency direction (shared → identity).

### ADR-011: BrekFoodApplicationTests uses @MockBean UserRepository
- **Decision**: `BrekFoodApplicationTests` declares `@MockBean UserRepository` to satisfy `AuthServiceImpl`'s dependency until Phase 1.3 delivers `JpaUserRepository`.
- **Rationale**: The smoke test must keep loading the full context; mocking one unimplemented dependency is cleaner than excluding the context test or using `@SpringBootTest(webEnvironment=NONE)`.

### ADR-012: UUID injection via reflection in unit tests
- **Decision**: Tests that need a non-null `User.getId()` inject a UUID via `BaseEntity.id` reflection (`Field.setAccessible(true)`).
- **Rationale**: JPA `@GeneratedValue(strategy = UUID)` only fires during persistence — unavailable in pure unit tests. Reflection avoids adding a `setId()` method or test-only constructor to the production domain entity.
- **Decision**: The field is named `passwordHash` (not `password`) in both Java and the DB column (`password_hash`).
- **Rationale**: Makes it explicit that only BCrypt hashes are ever stored; prevents accidental plain-text storage.
- **Decision**: Fairness rules are DOMAIN logic, not infrastructure
- **Rationale**: Fairness is BrekFood's competitive advantage - it belongs in the domain layer
- **Implications**: Pricing Engine, Earnings Engine, and Dispatch Algorithm are domain services

---

## 4. Domain Model Summary

### 4.1 Bounded Contexts

| Context      | Responsibility                                      | Priority |
|-------------|-----------------------------------------------------|----------|
| Identity     | User registration, login, roles, JWT                | P0       |
| Customer     | Customer profile, preferences                       | P0       |
| Restaurant   | Restaurant CRUD, menu items, availability           | P0       |
| Order        | Order lifecycle, items, status transitions           | P0       |
| Delivery     | Driver management, assignment, delivery tracking    | P1       |
| Payment      | Payment processing, webhooks                        | P1       |
| Pricing      | Fee calculation, surge pricing, loyalty discounts   | P1       |
| Earnings     | Driver earnings, fairness rules, distribution       | P1       |

### 4.2 Aggregates & Their Roots

| Aggregate   | Root Entity | Child Entities    | Key Value Objects              |
|-------------|-------------|-------------------|-------------------------------|
| Order       | Order       | OrderItem         | Money, Address, OrderStatus    |
| Delivery    | Delivery    | DriverAssignment  | Location, Distance             |
| Restaurant  | Restaurant  | MenuItem          | Price, Availability            |
| Driver      | Driver      | DriverStatus      | EarningsSnapshot               |

### 4.3 Value Objects Registry

| Value Object      | Fields                | Used By                    |
|-------------------|-----------------------|---------------------------|
| Money             | amount, currency      | Order, MenuItem, Earnings  |
| Distance          | km                    | Delivery, Pricing          |
| TimeWindow        | start, end            | Delivery, Restaurant       |
| Location          | lat, lng              | Delivery, Driver           |
| FeeBreakdown      | restaurant, delivery, platform | Order, Pricing    |
| EarningsBreakdown | base, distance, time, bonus | Earnings             |

### 4.4 Domain Events

| Event                  | Producer    | Consumers                  |
|-----------------------|-------------|---------------------------|
| OrderCreated          | Order       | Pricing, Restaurant        |
| OrderAccepted         | Restaurant  | Customer, Order            |
| OrderPrepared         | Restaurant  | Delivery                   |
| OrderReady            | Restaurant  | Delivery, Driver           |
| OrderAssignedToDriver | Delivery    | Customer, Driver, Order    |
| DeliveryStarted       | Delivery    | Customer, Order            |
| DeliveryCompleted     | Delivery    | Payment, Earnings, Order   |
| PaymentProcessed      | Payment     | Order, Earnings            |

---

## 5. API Contract Summary (v1)

**Base Path**: `/api/v1`

| Method | Endpoint                  | Context     | Description              |
|--------|---------------------------|-------------|--------------------------|
| POST   | /auth/register            | Identity    | User registration        |
| POST   | /auth/login               | Identity    | User login (JWT)         |
| GET    | /customers/me             | Customer    | Get current customer     |
| PUT    | /customers/me             | Customer    | Update current customer  |
| POST   | /restaurants              | Restaurant  | Create restaurant        |
| GET    | /restaurants              | Restaurant  | List restaurants         |
| GET    | /restaurants/{id}         | Restaurant  | Get restaurant by ID     |
| POST   | /orders                   | Order       | Create order             |
| GET    | /orders/{id}              | Order       | Get order by ID          |
| PATCH  | /orders/{id}/status       | Order       | Update order status      |
| POST   | /deliveries/assign        | Delivery    | Assign driver to order   |
| PATCH  | /deliveries/{id}/status   | Delivery    | Update delivery status   |
| POST   | /payments                 | Payment     | Process payment          |
| POST   | /payments/webhook         | Payment     | Payment gateway webhook  |

---

## 6. Core Engines (Differentiators)

### 6.1 Pricing Engine

```
platform_fee = (base_rate * order_value) + surge_multiplier - loyalty_discount
```
- `base_rate` range: [0.05, 0.15]
- Fee breakdown: restaurant_fee + delivery_fee + platform_fee
- Surge pricing: `surge = f(demand / supply)`
  - demand/supply > 1.5 -> surge = 1.3x
  - demand/supply > 2.0 -> surge = 1.6x

### 6.2 Earnings Engine

```
driver_earnings = base_fare + (distance_km * per_km_rate) + (time_min * per_min_rate) + bonus
```
- Dynamic: `E = E * demand_multiplier * fairness_multiplier`
- `fairness_multiplier = 1 + (avg_earnings - driver_earnings) / avg_earnings`
- Fairness rules:
  - Minimum guaranteed earnings per hour
  - No driver starvation (max idle time)
  - Balanced distribution

### 6.3 Dispatch Algorithm

```
score = w1 * proximity + w2 * idle_time + w3 * earnings_gap + w4 * acceptance_probability
```
- Dynamic weights: inequality up -> increase w3; delay up -> increase w1
- Anti-concentration: cap max orders per driver window, rotate assignments
- Order batching: combine orders with same route / close pickup points

---

## 7. Technology Stack (Planned)

| Layer              | Technology                        | Status      |
|-------------------|-----------------------------------|------------|
| Runtime           | Java 17                           | ✅ Active   |
| Framework         | Spring Boot 3.5.5                 | ✅ Active   |
| Persistence       | Spring Data JPA + PostgreSQL      | ✅ Active   |
| Security          | Spring Security + JWT (JJWT 0.12.6) | ✅ Phase 1.2 |
| Build             | Maven + mvnw                      | ✅ Active   |
| Coverage          | JaCoCo 0.8.12                     | ✅ Active (70/60/80% thresholds) |
| Utility           | Lombok                            | ✅ Active   |
| DTO Mapping       | MapStruct 1.6.3                   | ✅ Configured|
| Validation        | Bean Validation (Jakarta)         | ✅ Active   |
| Test DB           | H2 in-memory (profile: test)      | ✅ Active   |
| Integration Tests | Testcontainers + PostgreSQL 15    | ✅ Configured|
| Migration         | Flyway 10.x (Boot BOM)            | ✅ Active   |
| API Docs          | SpringDoc OpenAPI 2.8.8           | ✅ Active   |
| Containerization  | Docker + docker-compose           | ✅ Complete (Phase 0.4) |
| CI/CD             | GitHub Actions + SonarCloud       | ✅ Complete             |

---

## 8. Key Conventions

- **Package base**: `com.br.wstech.brekfood` ✅ active
- **Entity IDs**: UUID
- **Money**: Always use `BigDecimal` with scale 2, never `double`
- **Timestamps**: `Instant` (UTC), converted at presentation layer
- **Status enums**: Defined per aggregate (e.g., `OrderStatus`, `DeliveryStatus`)
- **DTOs**: Separate request/response records per endpoint
- **Validation**: Jakarta Bean Validation annotations on request DTOs
- **Error handling**: Global `@RestControllerAdvice` with standardized error responses

---

## 9. Evolution Roadmap

```
Phase 1: Modular Monolith
  -> All contexts in one deployable, separated by packages
  -> Synchronous communication between contexts
  -> Spring ApplicationEvent for domain events

Phase 2: Service Extraction
  -> Extract Delivery + Payment as separate services
  -> Introduce message broker (RabbitMQ/Kafka)
  -> API Gateway

Phase 3: Event-Driven Architecture
  -> Full event sourcing for Order aggregate
  -> CQRS for read-heavy queries (restaurant listings, order tracking)
  -> Real-time tracking via WebSocket/SSE
```

---

## 10. Risks & Mitigations

| Risk                                    | Mitigation                                  |
|-----------------------------------------|---------------------------------------------|
| Fairness algorithms add latency         | Pre-compute scores, cache driver states     |
| PostgreSQL bottleneck at scale          | Read replicas, CQRS separation later        |
| Driver location updates are high-volume | Consider Redis for real-time location cache |
| Payment webhook reliability             | Idempotency keys, retry queue               |
| Monolith coupling                       | Strict package boundaries, interface-based  |

---

## 11. Glossary

| Term               | Definition                                                        |
|--------------------|-------------------------------------------------------------------|
| Fairness Engine    | System ensuring equitable driver earnings and order distribution  |
| Surge Multiplier   | Dynamic price increase factor based on demand/supply ratio        |
| Dispatch Score     | Weighted score used to select optimal driver for an order         |
| Earnings Gap       | Difference between a driver's earnings and the fleet average      |
| Driver Starvation  | Situation where a driver receives no orders for extended period   |
| Order Batching     | Combining multiple orders into a single delivery route            |
| Gini Coefficient   | Statistical measure of earnings inequality among drivers          |

