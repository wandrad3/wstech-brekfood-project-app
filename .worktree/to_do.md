# BrekFood - TODO Tracker

> Last updated: 2026-03-31 (Phase 0.2 complete)
> Legend: `[ ]` pending | `[~]` in progress | `[x]` done | `[!]` blocked

---

## Phase 0 - Project Bootstrap & Infrastructure

### 0.1 Project Renaming & Setup
- [x] Rename artifact from `wstech-base-project` to `brekfood-app`
- [x] Rename base package from `com.br.wstech.wstech_base_project` to `com.br.wstech.brekfood`
- [x] Update `spring.application.name` to `brekfood`
- [x] Update `pom.xml` metadata (name, description, groupId)
- [x] Update README.md with BrekFood branding and project description

### 0.2 Dependencies to Add
- [x] Add Flyway for database migrations (`flyway-core` + `flyway-database-postgresql`)
- [x] Add SpringDoc OpenAPI (`springdoc-openapi-starter-webmvc-ui` 2.8.8)
- [x] Add Bean Validation (`spring-boot-starter-validation`)
- [x] Add MapStruct for DTO mapping (`mapstruct` 1.6.3 + `mapstruct-processor` + `lombok-mapstruct-binding`)
- [x] Add JJWT for JWT handling (`jjwt-api`, `jjwt-impl`, `jjwt-jackson` 0.12.6)
- [x] Add Testcontainers for integration tests (`spring-boot-testcontainers`, `junit-jupiter`, `postgresql`)
- [x] Add H2 for lightweight dev/test profile

### 0.3 Configuration
- [ ] Configure `application.properties` (or `.yml`) for PostgreSQL connection
- [ ] Create `application-dev.properties` profile
- [x] Create `application-test.properties` profile
- [x] Configure Flyway migration directory (`db/migration`) — `V0__baseline.sql` criado
- [x] Configure SpringDoc OpenAPI info (title, version, description) — `OpenApiConfig`
- [ ] Configure JaCoCo minimum coverage thresholds

### 0.4 Infrastructure
- [ ] Create `docker-compose.yml` with PostgreSQL 15 service
- [ ] Create `Dockerfile` for the Spring Boot app
- [ ] Create `.env.example` for environment variables
- [x] Setup global exception handler (`@RestControllerAdvice`) — `GlobalExceptionHandler`
- [x] Create standardized API error response model (`ApiError`, `ApiResponse<T>`)
- [x] Setup CORS configuration — `WebConfig`

---

## Phase 1 - Identity & Auth Context (P0)

### 1.1 Domain Layer
- [ ] Create `User` entity (id, email, password, name, role, createdAt, updatedAt)
- [ ] Create `Role` enum (CUSTOMER, RESTAURANT_OWNER, DRIVER, ADMIN)
- [ ] Create `UserRepository` interface

### 1.2 Application Layer
- [ ] Create `RegisterCommand` (email, password, name, role)
- [ ] Create `LoginCommand` (email, password)
- [ ] Create `AuthResponse` DTO (token, expiresAt, user)
- [ ] Create `AuthService` (register, login)
- [ ] Implement JWT token generation and validation
- [ ] Create `JwtTokenProvider` utility class

### 1.3 Infrastructure Layer
- [ ] Implement `JpaUserRepository`
- [ ] Create `JwtAuthenticationFilter` (OncePerRequestFilter)
- [ ] Configure `SecurityFilterChain` (permit auth endpoints, secure rest)
- [ ] Create `UserDetailsServiceImpl`
- [ ] Configure password encoding (BCrypt)

### 1.4 Interface Layer
- [ ] Create `POST /api/v1/auth/register` controller
- [ ] Create `POST /api/v1/auth/login` controller
- [ ] Add request validation annotations
- [ ] Add OpenAPI annotations

### 1.5 Database Migration
- [ ] Create `V1__create_users_table.sql`

### 1.6 Tests
- [ ] Unit test: AuthService (register, login, duplicate email)
- [ ] Unit test: JwtTokenProvider (generate, validate, expired)
- [ ] Integration test: Auth endpoints (register + login flow)
- [ ] Integration test: Security filter (protected endpoints return 401)

---

## Phase 2 - Customer Context (P0)

### 2.1 Domain Layer
- [ ] Create `Customer` entity (id, userId, phone, address, createdAt)
- [ ] Create `Address` value object (street, number, complement, neighborhood, city, state, zipCode, lat, lng)
- [ ] Create `CustomerRepository` interface

### 2.2 Application Layer
- [ ] Create `CustomerProfileResponse` DTO
- [ ] Create `UpdateCustomerRequest` DTO
- [ ] Create `CustomerService` (getProfile, updateProfile)

### 2.3 Interface Layer
- [ ] Create `GET /api/v1/customers/me` controller
- [ ] Create `PUT /api/v1/customers/me` controller

### 2.4 Database Migration
- [ ] Create `V2__create_customers_table.sql`

### 2.5 Tests
- [ ] Unit test: CustomerService
- [ ] Integration test: Customer endpoints (auth required)

---

## Phase 3 - Restaurant Context (P0)

### 3.1 Domain Layer
- [ ] Create `Restaurant` entity (id, ownerId, name, description, address, phone, category, active)
- [ ] Create `MenuItem` entity (id, restaurantId, name, description, price, category, available, imageUrl)
- [ ] Create `Price` value object (wrapping BigDecimal)
- [ ] Create `Availability` value object (openTime, closeTime, daysOfWeek)
- [ ] Create `RestaurantRepository` interface
- [ ] Create `MenuItemRepository` interface

### 3.2 Application Layer
- [ ] Create `CreateRestaurantRequest` / `RestaurantResponse` DTOs
- [ ] Create `CreateMenuItemRequest` / `MenuItemResponse` DTOs
- [ ] Create `RestaurantService` (create, list, getById)
- [ ] Create `MenuItemService` (addItem, updateItem, listByRestaurant)

### 3.3 Interface Layer
- [ ] Create `POST /api/v1/restaurants` controller
- [ ] Create `GET /api/v1/restaurants` controller (paginated)
- [ ] Create `GET /api/v1/restaurants/{id}` controller
- [ ] Create `POST /api/v1/restaurants/{id}/menu-items` controller
- [ ] Create `GET /api/v1/restaurants/{id}/menu-items` controller

### 3.4 Database Migration
- [ ] Create `V3__create_restaurants_table.sql`
- [ ] Create `V4__create_menu_items_table.sql`

### 3.5 Tests
- [ ] Unit test: RestaurantService
- [ ] Unit test: MenuItemService
- [ ] Integration test: Restaurant CRUD flow
- [ ] Integration test: MenuItem CRUD flow

---

## Phase 4 - Order Context (P0)

### 4.1 Domain Layer
- [ ] Create `Order` entity (id, customerId, restaurantId, status, totalAmount, createdAt, updatedAt)
- [ ] Create `OrderItem` entity (id, orderId, menuItemId, name, quantity, unitPrice, subtotal)
- [ ] Create `OrderStatus` enum (PENDING, ACCEPTED, PREPARING, READY, ASSIGNED, IN_DELIVERY, DELIVERED, CANCELLED)
- [ ] Create `Money` value object (amount: BigDecimal, currency: String)
- [ ] Create `OrderRepository` interface
- [ ] Define valid status transitions (state machine rules)

### 4.2 Domain Events
- [ ] Create `OrderCreated` event
- [ ] Create `OrderAccepted` event
- [ ] Create `OrderPrepared` event
- [ ] Create `OrderReady` event
- [ ] Setup Spring `ApplicationEventPublisher` integration

### 4.3 Application Layer
- [ ] Create `CreateOrderRequest` DTO (restaurantId, items[])
- [ ] Create `OrderResponse` DTO
- [ ] Create `UpdateOrderStatusRequest` DTO
- [ ] Create `OrderService` (create, getById, updateStatus)
- [ ] Implement order total calculation (fetch menu item prices)
- [ ] Implement status transition validation

### 4.4 Interface Layer
- [ ] Create `POST /api/v1/orders` controller
- [ ] Create `GET /api/v1/orders/{id}` controller
- [ ] Create `PATCH /api/v1/orders/{id}/status` controller
- [ ] Create `GET /api/v1/orders` (list my orders - customer/restaurant)

### 4.5 Database Migration
- [ ] Create `V5__create_orders_table.sql`
- [ ] Create `V6__create_order_items_table.sql`

### 4.6 Tests
- [ ] Unit test: OrderService (create, status transitions, invalid transitions)
- [ ] Unit test: Money value object
- [ ] Unit test: Order total calculation
- [ ] Integration test: Full order lifecycle
- [ ] Integration test: Order status state machine

---

## Phase 5 - Pricing Engine (P1 - Core Differentiator)

### 5.1 Domain Layer
- [ ] Create `FeeBreakdown` value object (restaurantFee, deliveryFee, platformFee)
- [ ] Create `PricingRule` entity (baseRate, surgeMultiplier, loyaltyDiscount)
- [ ] Create `PricingService` domain service
- [ ] Implement base formula: `platform_fee = base_rate * order_value`
- [ ] Implement advanced formula: `platform_fee = (base_rate * order_value) + surge - loyalty`
- [ ] Implement surge pricing model: `surge = f(demand / supply)`

### 5.2 Application Layer
- [ ] Create `CalculateFeeRequest` DTO
- [ ] Create `FeeBreakdownResponse` DTO
- [ ] Create `PricingApplicationService`

### 5.3 Integration with Order
- [ ] Listen to `OrderCreated` event to calculate fees
- [ ] Attach fee breakdown to order

### 5.4 Tests
- [ ] Unit test: PricingService (base formula, surge, loyalty)
- [ ] Unit test: Surge pricing thresholds (1.3x, 1.6x)
- [ ] Unit test: FeeBreakdown calculation
- [ ] Integration test: Order creation triggers pricing

---

## Phase 6 - Delivery & Driver Context (P1)

### 6.1 Domain Layer
- [ ] Create `Driver` entity (id, userId, name, phone, vehicleType, status, location)
- [ ] Create `DriverStatus` enum (AVAILABLE, ASSIGNED, DELIVERING, OFFLINE)
- [ ] Create `Delivery` entity (id, orderId, driverId, status, pickupLocation, dropoffLocation, startedAt, completedAt)
- [ ] Create `DeliveryStatus` enum (PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED)
- [ ] Create `Location` value object (lat, lng)
- [ ] Create `Distance` value object (km)
- [ ] Create `DriverAssignment` entity (driverId, deliveryId, score, assignedAt)
- [ ] Create `DriverRepository` / `DeliveryRepository` interfaces

### 6.2 Dispatch Algorithm (Core)
- [ ] Create `DispatchService` domain service
- [ ] Implement scoring function: `score = w1*proximity + w2*idle_time + w3*earnings_gap + w4*acceptance`
- [ ] Implement anti-concentration rules (max orders per window)
- [ ] Implement driver rotation logic
- [ ] Implement order batching (same route / close pickups)

### 6.3 Application Layer
- [ ] Create `AssignDriverRequest` / `DeliveryResponse` DTOs
- [ ] Create `UpdateDeliveryStatusRequest` DTO
- [ ] Create `DeliveryApplicationService`

### 6.4 Domain Events
- [ ] Create `OrderAssignedToDriver` event
- [ ] Create `DeliveryStarted` event
- [ ] Create `DeliveryCompleted` event

### 6.5 Interface Layer
- [ ] Create `POST /api/v1/deliveries/assign` controller
- [ ] Create `PATCH /api/v1/deliveries/{id}/status` controller
- [ ] Create `GET /api/v1/deliveries/{id}` controller

### 6.6 Database Migration
- [ ] Create `V7__create_drivers_table.sql`
- [ ] Create `V8__create_deliveries_table.sql`
- [ ] Create `V9__create_driver_assignments_table.sql`

### 6.7 Tests
- [ ] Unit test: DispatchService scoring function
- [ ] Unit test: Anti-concentration rules
- [ ] Unit test: Order batching logic
- [ ] Unit test: Distance calculation
- [ ] Integration test: Full delivery assignment flow
- [ ] Integration test: Delivery status lifecycle

---

## Phase 7 - Earnings Engine (P1 - Core Differentiator)

### 7.1 Domain Layer
- [ ] Create `EarningsRecord` entity (id, driverId, orderId, base, distanceEarning, timeEarning, bonus, total, calculatedAt)
- [ ] Create `EarningsBreakdown` value object (base, distance, time, bonus)
- [ ] Create `EarningsSnapshot` value object (totalToday, avgPerHour, ordersCount)
- [ ] Create `EarningsService` domain service
- [ ] Implement base formula: `E = base + (d * r_km) + (t * r_min) + bonus`
- [ ] Implement dynamic adjustments: `E = E * demand_multiplier * fairness_multiplier`
- [ ] Implement fairness_multiplier: `1 + (avg_earnings - driver_earnings) / avg_earnings`

### 7.2 Fairness Rules
- [ ] Implement minimum guaranteed earnings per hour
- [ ] Implement driver starvation prevention (max idle time trigger)
- [ ] Implement balanced distribution check

### 7.3 Application Layer
- [ ] Create `EarningsResponse` / `EarningsSnapshotResponse` DTOs
- [ ] Create `EarningsApplicationService`
- [ ] Listen to `DeliveryCompleted` event to calculate earnings

### 7.4 Database Migration
- [ ] Create `V10__create_earnings_table.sql`

### 7.5 Tests
- [ ] Unit test: EarningsService (base formula)
- [ ] Unit test: Fairness multiplier calculation
- [ ] Unit test: Minimum guarantee enforcement
- [ ] Unit test: Starvation prevention trigger
- [ ] Integration test: Delivery completed triggers earnings calculation

---

## Phase 8 - Payment Context (P1)

### 8.1 Domain Layer
- [ ] Create `Payment` entity (id, orderId, amount, method, status, gatewayTransactionId, createdAt)
- [ ] Create `PaymentStatus` enum (PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED)
- [ ] Create `PaymentRepository` interface

### 8.2 Application Layer
- [ ] Create `CreatePaymentRequest` / `PaymentResponse` DTOs
- [ ] Create `PaymentService` (process, handleWebhook)
- [ ] Implement idempotency key handling

### 8.3 Domain Events
- [ ] Create `PaymentProcessed` event

### 8.4 Interface Layer
- [ ] Create `POST /api/v1/payments` controller
- [ ] Create `POST /api/v1/payments/webhook` controller (no auth, signature verification)

### 8.5 Database Migration
- [ ] Create `V11__create_payments_table.sql`

### 8.6 Tests
- [ ] Unit test: PaymentService (process, webhook handling)
- [ ] Unit test: Idempotency key logic
- [ ] Integration test: Payment + webhook flow

---

## Phase 9 - Cross-Cutting Concerns

### 9.1 Observability
- [ ] Add Spring Boot Actuator
- [ ] Configure health check endpoints
- [ ] Add structured logging (correlation IDs)
- [ ] Add Micrometer metrics for core engines (pricing latency, dispatch time, earnings calculations)

### 9.2 API Documentation
- [ ] Add OpenAPI annotations to all controllers
- [ ] Document all request/response schemas
- [ ] Document error responses
- [ ] Add example values to schemas

### 9.3 Security Hardening
- [ ] Rate limiting on auth endpoints
- [ ] Input sanitization
- [ ] Webhook signature verification for payments
- [ ] Role-based access control (CUSTOMER, RESTAURANT_OWNER, DRIVER, ADMIN)
- [ ] Endpoint authorization matrix

### 9.4 Data Integrity
- [ ] Add database constraints (unique, not null, check)
- [ ] Add database indexes for frequent queries
- [ ] Add optimistic locking on concurrent-access entities (Order, Delivery)

---

## Phase 10 - Simulation Engine (Future)

### 10.1 Core Simulation
- [ ] Create discrete time-step simulator (1-minute intervals, 24h cycles)
- [ ] Implement Poisson demand generation with time-of-day variation
- [ ] Implement driver behavior model (location, status, fatigue, acceptance probability)
- [ ] Implement geographic zone clustering (grid/hex-based)

### 10.2 Scenarios
- [ ] Scenario A: High Demand stress test
- [ ] Scenario B: Driver Scarcity validation
- [ ] Scenario C: Fairness Stress (uneven earnings convergence)

### 10.3 Metrics Collection
- [ ] Track avg delivery time, order completion rate
- [ ] Track earnings variance, Gini coefficient
- [ ] Track driver churn risk proxy, idle time distribution

### 10.4 Adaptive Learning
- [ ] Implement dynamic weight adjustment for dispatch
- [ ] Implement reinforcement-like parameter updates
- [ ] Implement reward function (minimize delivery time + minimize earnings variance)

---

## Dependency Graph

```
Phase 0 (Bootstrap)
  |
  v
Phase 1 (Identity/Auth) ----+
  |                          |
  v                          v
Phase 2 (Customer)      Phase 3 (Restaurant)
  |                          |
  +----------+---------------+
             |
             v
        Phase 4 (Order) <---- Phase 5 (Pricing)
             |
             v
        Phase 6 (Delivery + Dispatch)
             |
             +----> Phase 7 (Earnings)
             |
             +----> Phase 8 (Payment)
             |
             v
        Phase 9 (Cross-Cutting)
             |
             v
        Phase 10 (Simulation)
```

---

## Notes & Decisions Log

| Date       | Decision                                                                   |
|------------|----------------------------------------------------------------------------|
| 2026-03-31 | Project initialized from wstech-base-project Spring Boot template          |
| 2026-03-31 | Created plan.md with full DDD specification                                |
| 2026-03-31 | Created memory.md and to_do.md for project tracking                        |
| 2026-03-31 | Decision: Start Phase 0 (bootstrap) before any domain code                 |
| 2026-03-31 | **Phase 0.1 COMPLETE** — Renamed to brekfood-app, new package structure    |
| 2026-03-31 | Created full DDD package skeleton (8 bounded contexts × 4 layers each)     |
| 2026-03-31 | Created Shared Kernel: BaseEntity, DomainException hierarchy, GlobalExceptionHandler, ApiResponse, ApiError, JpaConfig, WebConfig |
| 2026-03-31 | Test profile configured with H2 in-memory — no PostgreSQL needed for tests |
| 2026-03-31 | 18 unit tests passing (BUILD SUCCESS)                                      |
| 2026-03-31 | **Phase 0.2 COMPLETE** — All Phase 0.2 dependencies added and configured    |
| 2026-03-31 | Added: Flyway, SpringDoc OpenAPI 2.8.8, Bean Validation, MapStruct 1.6.3, JJWT 0.12.6, Testcontainers |
| 2026-03-31 | Created: OpenApiConfig (JWT Bearer scheme + 8 API tags), JwtProperties (validated @ConfigurationProperties) |
| 2026-03-31 | Created: AbstractIntegrationTest (Testcontainers PostgreSQL base class) |
| 2026-03-31 | Created: Flyway migration dir + V0__baseline.sql |
| 2026-03-31 | 23 unit tests passing (BUILD SUCCESS) — 5 new tests for JwtProperties validation |

