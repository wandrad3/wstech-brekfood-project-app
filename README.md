# BrekFood

> **Fairness-first delivery platform** — transparent fees, fair driver earnings, fast UX.

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.5-brightgreen?logo=springboot)
![Java](https://img.shields.io/badge/Java-17-blue?logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.9.x-C71A36?logo=apachemaven)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)
![JaCoCo](https://img.shields.io/badge/JaCoCo-70%25_line-yellow)
![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?logo=springsecurity)
![Build](https://img.shields.io/badge/build-passing-brightgreen)
![Phase](https://img.shields.io/badge/phase-1.2_complete-success)

---

## What is BrekFood?

BrekFood is not just a delivery app. It is:

- **A Pricing System** — transparent, fair fees for restaurants (5–15% range, no hidden charges)
- **An Earnings Engine** — guaranteed minimum earnings, fairness multiplier, anti-starvation rules
- **A Dispatch Algorithm** — score-based driver assignment that balances proximity, idle time, and earnings equity

This is the true competitive advantage over existing platforms.

---

## Architecture

BrekFood is structured as a **modular monolith** following **Domain-Driven Design (DDD)**, with clear package boundaries per bounded context. Each context is self-contained and follows the same layered structure:

```
src/main/java/com/br/wstech/brekfood/
│
├── shared/                         # Shared Kernel — cross-cutting concerns
│   ├── domain/
│   │   ├── model/BaseEntity.java   # Base UUID entity with auditing
│   │   └── exception/              # DomainException, EntityNotFoundException,
│   │                               #   BusinessRuleViolationException
│   ├── infrastructure/
│   │   └── config/                 # JpaConfig (auditing), WebConfig (CORS)
│   └── interfaces/rest/
│       ├── response/               # ApiResponse<T>, ApiError
│       └── exception/              # GlobalExceptionHandler
│
├── identity/                       # Auth, JWT, User roles
├── customer/                       # Customer profiles
├── restaurant/                     # Restaurants & menus
├── order/                          # Order lifecycle & state machine
├── delivery/                       # Driver management & dispatch algorithm
├── pricing/                        # Fee calculation & surge pricing engine
├── payment/                        # Payment processing & webhooks
└── earnings/                       # Driver earnings & fairness engine
```

Each bounded context follows this internal structure:
```
<context>/
  domain/         → Entities, Value Objects, Domain Events, Repository interfaces
  application/    → Use Cases, Application Services, Commands/Queries, DTOs
  infrastructure/ → JPA Repositories, External Clients, Event Publishers
  interfaces/     → REST Controllers, Request/Response models
```

### Evolution Path

```
Phase 1 (Now)   → Modular Monolith (package-level separation)
Phase 2         → Extract Delivery + Payment as separate services
Phase 3         → Full Event-Driven Architecture (Kafka + CQRS)
```

---

## Core Engines

### Pricing Engine
```
platform_fee = (base_rate × order_value) + surge_multiplier − loyalty_discount
```
- `base_rate` ∈ [0.05, 0.15]
- Surge: `demand/supply > 1.5 → 1.3×` | `demand/supply > 2.0 → 1.6×`

### Earnings Engine
```
driver_earnings = base_fare + (distance_km × r_km) + (time_min × r_min) + bonus
driver_earnings = driver_earnings × demand_multiplier × fairness_multiplier
```
- `fairness_multiplier = 1 + (avg_earnings − driver_earnings) / avg_earnings`
- Minimum guaranteed earnings per hour enforced
- Anti-starvation: max idle time trigger

### Dispatch Algorithm
```
score = w1×proximity + w2×idle_time + w3×earnings_gap + w4×acceptance_probability
```
- Dynamic weights — if inequality ↑, increase `w3`; if delay ↑, increase `w1`
- Anti-concentration: cap max orders per driver per time window

---

## Tech Stack

| Layer         | Technology                    | Status       |
|---------------|-------------------------------|--------------|
| Runtime       | Java 17                       | ✅ Active     |
| Framework     | Spring Boot 3.5.5             | ✅ Active     |
| Persistence   | Spring Data JPA + PostgreSQL  | ✅ Active     |
| Security      | Spring Security 6 + JJWT 0.12.6 | ✅ Phase 1.2 |
| Build         | Maven + mvnw                  | ✅ Active     |
| Coverage      | JaCoCo 0.8.12 (≥70% line)    | ✅ Active     |
| Validation    | Bean Validation (Jakarta)     | ✅ Active     |
| DTO Mapping   | MapStruct 1.6.3               | ✅ Configured |
| Migration     | Flyway 10.x                   | ✅ Active     |
| API Docs      | SpringDoc OpenAPI 2.8.8       | ✅ Active     |
| Test DB       | H2 (in-memory, test profile)  | ✅ Active     |
| Int. Tests    | Testcontainers + PostgreSQL   | ✅ Configured |
| Containers    | Docker + Compose              | ✅ Complete   |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+ (or use `./mvnw`)
- Docker & Docker Compose (for full stack)

### Quick Start — Docker Compose (recommended)

```bash
# 1. Copy and configure environment variables
cp .env.example .env
# Edit .env and set DB_PASSWORD and JWT_SECRET to strong values

# 2. Start everything (PostgreSQL + Spring Boot app)
docker-compose up -d

# 3. Check health
docker-compose ps
```

- Swagger UI → http://localhost:8080/swagger-ui.html
- OpenAPI JSON → http://localhost:8080/api-docs

---

### Run Database Only (for local development)

```bash
# Start only PostgreSQL
docker-compose up -d postgres

# Run the app with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

### Running Tests (no Docker required)

```bash
# Unit tests — H2 in-memory, no PostgreSQL needed
./mvnw clean test -Dspring.profiles.active=test

# Full verify (unit tests + JaCoCo coverage gate)
./mvnw clean verify -Dspring.profiles.active=test
```

> **Coverage thresholds** enforced at `mvn verify`: LINE ≥ 70%, BRANCH ≥ 60%, CLASS ≥ 80%

---

### Build Docker Image manually

```bash
./mvnw clean package -DskipTests
docker build -t brekfood-app:latest .
```

---

### Environment Variables

| Variable              | Default                             | Description                              |
|-----------------------|-------------------------------------|------------------------------------------|
| `DB_HOST`             | `localhost`                         | PostgreSQL hostname                      |
| `DB_PORT`             | `5432`                              | PostgreSQL port                          |
| `DB_NAME`             | `brekfood`                          | Database name                            |
| `DB_USER`             | `brekfood`                          | Database user                            |
| `DB_PASSWORD`         | `brekfood`                          | Database password (**change in prod**)   |
| `JWT_SECRET`          | *(dev fallback)*                    | JWT signing secret (≥32 chars required)  |
| `JWT_EXPIRATION_MS`   | `86400000` (24h)                    | Token TTL in milliseconds                |
| `CORS_ALLOWED_ORIGINS`| `http://localhost:3000`             | Allowed CORS origins (comma-separated)   |
| `SERVER_PORT`         | `8080`                              | Application port                         |

> See `.env.example` for the full reference template.

---

## API Overview (v1)

**Base URL**: `/api/v1`

| Method | Endpoint                  | Context    | Auth   |
|--------|---------------------------|------------|--------|
| POST   | /auth/register            | Identity   | Public |
| POST   | /auth/login               | Identity   | Public |
| GET    | /customers/me             | Customer   | JWT    |
| PUT    | /customers/me             | Customer   | JWT    |
| GET    | /restaurants              | Restaurant | Public |
| GET    | /restaurants/{id}         | Restaurant | Public |
| POST   | /restaurants              | Restaurant | JWT    |
| POST   | /orders                   | Order      | JWT    |
| GET    | /orders/{id}              | Order      | JWT    |
| PATCH  | /orders/{id}/status       | Order      | JWT    |
| POST   | /deliveries/assign        | Delivery   | JWT    |
| PATCH  | /deliveries/{id}/status   | Delivery   | JWT    |
| POST   | /payments                 | Payment    | JWT    |
| POST   | /payments/webhook         | Payment    | Signed |

---

## Error Responses

All API errors follow a consistent format:

```json
{
  "timestamp": "2026-03-31T20:00:00Z",
  "status": 422,
  "error": "BUSINESS_RULE_VIOLATION",
  "message": "Order status transition from PENDING to DELIVERED is not allowed",
  "path": "/api/v1/orders/abc/status"
}
```

Validation errors include field-level details:
```json
{
  "timestamp": "2026-03-31T20:00:00Z",
  "status": 400,
  "error": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "path": "/api/v1/auth/register",
  "fieldErrors": [
    { "field": "email", "message": "must not be blank" }
  ]
}
```

---

## CI/CD Pipeline

Every push to `feature/**`, `hotfix/**`, or `bugfix/**` branches triggers:

| Step | Job | What it does |
|------|-----|-------------|
| 1 | **Build & Test** | `mvn clean verify` — compiles, runs 116 unit tests, enforces JaCoCo gate |
| 2 | **SonarCloud** | Coverage upload, bug detection, CVE vulnerability scan, code smell analysis |
| 3 | **Auto PR** | Creates/updates a Pull Request to `develop` with CI summary |

### Required Setup

1. **SonarCloud**: Create project at https://sonarcloud.io
2. **GitHub Secrets**: `SONAR_TOKEN`
3. **GitHub Variables**: `SONAR_PROJECT_KEY`, `SONAR_ORGANIZATION`

> See `skills/skill-ci-pipeline.md` for full setup, troubleshooting, and branching model.

---

## Project Tracking

| File                          | Purpose                              |
|-------------------------------|--------------------------------------|
| `.worktree/plan.md`           | Full DDD specification & domain model |
| `.worktree/memory.md`         | Architecture decisions & current state |
| `.worktree/to_do.md`          | Phase-by-phase task tracker          |
| `skills/skill-ci-pipeline.md`      | CI pipeline setup & operations  |
| `skills/skill-stack-upgrade.md`    | Stack upgrade runbook           |
| `skills/skill-gc-troubleshooting.md` | GC tuning & troubleshooting   |

---

## License

Proprietary — WSTech Solutions © 2026
