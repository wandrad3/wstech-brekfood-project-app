# BrekFood — Postman Collection

Manual test collection for the BrekFood REST API.

## Files

| File | Purpose |
|------|---------|
| `BrekFood.postman_collection.json` | Full request collection (v2.1) |
| `BrekFood.postman_environment.json` | Local-dev environment variables |

## Import into Postman

1. Open Postman → **Import** (top-left)
2. Drag-and-drop **both** JSON files (or import them one by one)
3. Select the **"BrekFood — Local Dev"** environment in the top-right dropdown

## Quick Start

> **Pre-requisite**: the application must be running on `http://localhost:8080`
> ```bash
> ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
> ```

### Happy-path flow (recommended order)

1. `🔓 Auth — Register > [201] Register as Customer` — creates a user, **saves JWT** to `jwt_token`
2. `🔓 Auth — Login > [200] Login — valid credentials` — logs in, **refreshes JWT**
3. `🔒 Security — Protected Endpoints > [404] Valid token passes security filter` — proves JWT is accepted

### Variables populated automatically

| Variable | Set by |
|----------|--------|
| `jwt_token` | Register (201) or Login (200) test script |
| `registered_email` | Register (201) test script |

---

## Folder structure

```
BrekFood API — Epic 1 (Identity & Auth)
│
├── 🔓 Auth — Register
│   ├── [201] Register as Customer (happy path)       ← saves jwt_token
│   ├── [201] Register as Restaurant Owner
│   ├── [201] Register as Driver
│   ├── [201] Register as Admin
│   ├── [422] Register — duplicate email
│   ├── [400] Validation — blank email
│   ├── [400] Validation — invalid email format
│   ├── [400] Validation — password too short (< 8 chars)
│   ├── [400] Validation — blank name
│   ├── [400] Validation — null role
│   └── [400] Validation — invalid role value
│
├── 🔓 Auth — Login
│   ├── [200] Login — valid credentials (happy path)  ← refreshes jwt_token
│   ├── [401] Login — wrong password
│   ├── [401] Login — unknown email
│   ├── [400] Validation — blank email
│   └── [400] Validation — blank password
│
├── 🔒 Security — Protected Endpoints
│   ├── [401] No token — unauthenticated request
│   ├── [401] Malformed Bearer token
│   ├── [401] Tampered JWT signature
│   └── [404] Valid token passes security filter      ← uses jwt_token
│
└── 📋 API Docs
    ├── Swagger UI
    └── OpenAPI JSON spec
```

## Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `base_url` | `http://localhost:8080` | API host — change for staging/prod |
| `jwt_token` | *(empty)* | Bearer token — auto-filled by test scripts |
| `registered_email` | *(empty)* | Last registered e-mail — auto-filled |

## Notes

- **Register happy-path** uses a timestamp in the email (`customer_<ts>@brekfood.com`) to avoid duplicate-email conflicts on repeated runs.
- **Duplicate email** test uses the hardcoded `duplicate@brekfood.com` — run it twice to trigger 422, or register that email first manually.
- All **validation error** requests expect `400` with `error: "VALIDATION_FAILED"` and a `fieldErrors` array.
- **Protected endpoints** use `/api/v1/customers/me` as a proxy to verify JWT filter behaviour. The endpoint is not implemented yet, so the expected response with a valid token is `404 NOT_FOUND` (not `401`).

