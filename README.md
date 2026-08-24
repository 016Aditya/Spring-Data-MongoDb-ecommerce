🛒 ShopApp Backend

Spring Boot + MongoDB REST API powering the ShopApp e-commerce platform.

A stateless, production-oriented backend covering the complete commerce flow — authentication, products, cart, checkout, orders, returns, reviews, wishlist, and addresses.


✨ What This Backend Does

ShopApp is a stateless REST API consumed by the companion React frontend. It intentionally goes beyond basic CRUD and implements several production-style backend patterns:

Capability

What it provides

🛍️ E-commerce Core

Products, cart, checkout, orders, reviews, wishlist & addresses

📦 Atomic Inventory

Race-safe stock decrements to prevent overselling

🔐 Login Hardening

Rate limiting, lockouts, progressive delays & CAPTCHA escalation

🧾 Order Snapshots

Historical orders remain correct even if products change

🔎 Relevance Search

Tokenized search, synonyms and in-memory ranking

🔄 Return Workflow

Explicit order-return/refund state machine

⚠️ Consistent Errors

Structured JSON errors with domain-specific codes

📚 Table of Contents



Overview

Tech stack

Architecture

Project structure

Data model

Security architecture

Key design decisions

API reference

Error handling

Configuration

Getting started

Known limitations / roadmap

Overview

This is the backend for ShopApp, a full e-commerce platform (catalog, cart, checkout, orders/returns, reviews, wishlist, addresses). It's a stateless REST API — no server-rendered views, no sessions — designed to be consumed by the companion React frontend (Ecommerce-Frontend-Reactjs).

Beyond CRUD, the service implements a few things a production store actually needs:

Atomic, race-safe stock decrements at checkout (no overselling under concurrent orders)

A brute-force-resistant login flow (lockouts, progressive delay, CAPTCHA escalation)

Immutable order history via product snapshots

A custom in-memory relevance-ranked product search with synonym expansion

A full order return/refund state machine

Tech stack

Layer

Technology

Language / runtime

Java 21

Framework

Spring Boot 4.0.7

Database

MongoDB (Spring Data MongoDB)

Auth

Spring Security (stateless) + JJWT 0.12.6

Rate limiting

Bucket4j 8.16.0

Bot protection

Cloudflare Turnstile (server-side verification)

Object mapping

ModelMapper 3.2.6

Boilerplate reduction

Lombok

Build

Maven

🏗️ Architecture

The service follows a conventional layered architecture, with one deliberate twist: inventory mutation is pulled out of the order flow into its own service so it can be atomic and independently testable.

graph TB
    subgraph Client
        FE[React frontend]
    end

    subgraph "Spring Boot backend"
        FILTER[JwtAuthFilter]
        CTRL[REST controllers]
        SVC[Domain services]
        INV[InventoryService<br/>atomic stock ops]
        REPO[Spring Data repositories]
    end

    DB[(MongoDB<br/>ecommerce_db)]
    CF[Cloudflare Turnstile]

    FE -->|HTTPS + Bearer JWT| FILTER
    FILTER --> CTRL
    CTRL --> SVC
    SVC --> INV
    SVC --> REPO
    INV --> REPO
    REPO --> DB
    SVC -.CAPTCHA verify.-> CF

Request flow for a typical write (e.g. checkout):

JwtAuthFilter validates the bearer token and populates SecurityContext (or short-circuits with 401).

SecurityConfig's authorization rules decide if the route is even reachable for this principal.

The controller deserializes the request DTO, delegates to a service — controllers contain no business logic.

The service orchestrates: validation → mutation → persistence, delegating anything inventory-related to InventoryService.

GlobalExceptionHandler catches any domain exception thrown along the way and converts it into a consistent ApiErrorResponse JSON body with the right HTTP status.

🔁 Request Lifecycle at a Glance

React Client
    │
    │ HTTPS + Bearer JWT
    ▼
JwtAuthFilter
    │
    ▼
SecurityConfig
    │
    ▼
REST Controller
    │
    ▼
Domain Service
    ├──────────────► InventoryService
    │                    │
    │                    ▼
    │              MongoDB Repository
    │
    ▼
MongoDB Repository
    │
    ▼
MongoDB

Errors ───────────► GlobalExceptionHandler ───► ApiErrorResponse


## 📁 Project Structure


learnSpringMongoDb/
└── src/main/java/learnMongoDb/learnSpringMongoDb/
    ├── config/          # Security, CORS, Mongo, ModelMapper, rate limiter, seeder, synonyms
    ├── controller/      # REST endpoints — thin, no business logic
    ├── dto/             # Request/response shapes
    │   └── response/    # Shared response envelopes
    ├── entity/          # @Document-annotated MongoDB models
    ├── error/           # Custom exceptions + GlobalExceptionHandler
    ├── repository/      # Spring Data MongoRepository interfaces
    ├── security/        # JWT filter, JWT util, CustomUserDetails
    └── service/         # Business logic
        └── sync/        # Guest → authenticated data migration

Naming convention: DTOs are nested classes inside a per-domain DTO file (e.g. OrderDto.Response, OrderDto.CheckoutRequest) rather than one top-level class per shape — keeps related request/response pairs colocated.

🗄️ Data Model

7 MongoDB collections in the ecommerce_db database:

erDiagram
    USERS ||--o{ ORDERS : places
    USERS ||--o| CARTS : owns
    USERS ||--o| WISHLISTS : owns
    USERS ||--o{ ADDRESSES : saves
    USERS ||--o{ REVIEWS : writes
    PRODUCTS ||--o{ REVIEWS : receives
    ORDERS }o--o{ PRODUCTS : "snapshots at purchase time"

    USERS {
        string id PK
        string email UK
        string passwordHash
        string role
        int failedLoginAttempts
        instant lockedUntil
    }
    PRODUCTS {
        string id PK
        string name
        string category
        double price
        int stock
        boolean inStock
        double averageRating
    }
    ORDERS {
        string id PK
        string userId FK
        string status
        double totalPrice
        list items
        string refundStatus
    }
    CARTS {
        string id PK
        string userId FK "unique"
        list items
        double cartTotal
    }
    ADDRESSES {
        string id PK
        string userId FK
        boolean defaultAddress
    }
    REVIEWS {
        string id PK
        string productId FK
        string userId FK
        int rating
    }
    WISHLISTS {
        string id PK
        string userId FK
        list productIds
    }

Notable fields:

User — carries its own security state (failedLoginAttempts, lockoutCount, lockedUntil, nextLoginAllowedAt) rather than delegating to a separate auth table. passwordHash is always BCrypt (strength 12); the raw password never touches the database.

Product — averageRating / totalRatings are write-only from ReviewService; inStock is write-only from InventoryService. Nothing else should ever set these fields directly.

Order — see Order snapshot pattern below. Also carries returnRequestedAt, returnCompletedAt, refundStatus — all nullable, added additively without a data migration.

ShoppingCart — one cart per user (userId has a unique index), embeds CartItems directly rather than referencing a separate collection.

Wishlist — just a userId → List<String> of product IDs; deliberately the simplest entity in the system.

🔐 Security Architecture

Authentication is stateless JWT — no HTTP sessions, no server-side auth state beyond what's in Mongo for lockouts.

sequenceDiagram
    participant C as Client
    participant F as JwtAuthFilter
    participant S as SecurityContext
    participant Ctrl as Controller

    C->>F: Request + Authorization: Bearer <jwt>
    alt no header
        F->>Ctrl: pass through (anonymous)
    else invalid / expired token
        F-->>C: 401 Unauthorized (JSON body)
    else valid token
        F->>S: set CustomUserDetails principal
        F->>Ctrl: continue chain
    end

Login hardening (UserService.loginUser) runs a fixed sequence of checks before a password comparison even happens:

Step

Mechanism

Configuration

1

IP + email rate limit (Bucket4j)

5 requests / minute

2

Hard lockout check

15 min (1 min on first offense, doubling lockout count thereafter)

3

Progressive delay

2s × 2^(attempts-2), capped at 30s

4

CAPTCHA requirement

Triggered after 3 failed attempts; verified server-side against Cloudflare

5

Password check (BCrypt)

Constant-time: unknown emails still run a dummy hash comparison to avoid timing leaks

6

Counter reset

All failure counters cleared atomically on success

Failed-login bookkeeping uses MongoDB's atomic findAndModify (via MongoTemplate) rather than read-modify-write, so concurrent failed attempts from the same account can't race each other into an inconsistent counter.

JWT claims: sub (userId), email, role, fullName. Expiry: 7 days (jwt.expiration-ms=604800000), configurable via env var.

Authorization rules (SecurityConfig): registration, login, and password-recovery endpoints are public; product catalog reads (GET /api/products/**) and review reads are public for guest browsing; everything else — including all of /api/v1/addresses/** — requires a valid JWT. Unauthenticated and forbidden requests get structured JSON (401/403) instead of Spring's default HTML error page.

CORS is locked to explicit origins (localhost:5173, localhost:4173, and the configured production frontend URL) rather than a wildcard, with credentials enabled.

🧠 Key Design Decisions

1. Orders store snapshots, not references

OrderItem copies productName, productImage, and price at the moment of purchase instead of holding a @DBRef to Product. Consequences:

Editing or deleting a product later never corrupts historical orders.

Rendering an order requires zero secondary lookups — no N+1 queries.

This is the same pattern used by Shopify/Amazon-style platforms, and it's why Order.legacyProducts exists: a @Field("products") alias that lets pre-migration documents (which stored snapshots under the old field name) deserialize correctly with no backfill required. ModelMapper's Order → OrderDto.Response type map prefers items and falls back to legacyProducts automatically.

2. Inventory mutation is atomic and self-healing

InventoryService.atomicDecreaseStock uses a single MongoDB findAndModify with stock >= quantity as part of the query filter — the database itself rejects the update if stock is insufficient, so there's no read-then-write race window under concurrent checkouts.

For multi-item checkouts, atomicDecreaseStockBatch decrements items one at a time, pushing each success onto a stack. If any item in the batch fails, everything already deducted is rolled back (atomicIncreaseStock) before the exception propagates — a hand-rolled compensating-transaction pattern. OrderService.checkout wraps this a second time: if the order document itself fails to persist after stock was deducted, the whole batch is rolled back again.

3. Checkout is a pure orchestrator

OrderService intentionally contains zero inventory logic. Validation is delegated to InventoryValidationService (product exists → in stock → quantity ≤ stock → quantity ≤ maxOrderQuantity), mutation to InventoryService. This split exists so each concern can be tested and reasoned about independently — the code comments explicitly track this as a refactor ("Commit 3: pure orchestrator, zero inventory logic").

4. Custom relevance-ranked search

ProductQueryService doesn't delegate to MongoDB text search. It builds AND-of-OR regex criteria per query token, expands each token through a hand-maintained synonym table (phone ↔ mobile ↔ smartphone, tv ↔ television, etc.), pulls a capped candidate pool (300 docs) from Mongo, then ranks in Java:

Name match (prefix/word-boundary) outweighs brand, which outweighs category

Featured products get a small boost

Higher-rated products get a small boost

Out-of-stock products are penalized to the bottom; critically-low-stock items lose part of their in-stock boost

Pagination happens after ranking, on the sorted in-memory list.

5. Order returns are a real state machine

Orders move through an explicit lifecycle, each transition validated against the required prior state:


`PENDING → DELIVERED → RETURN_REQUESTED → RETURN_APPROVED → PICKUP_SCHEDULED → PICKED_UP → REFUND_PROCESSED → RETURN_SUCCESSFUL`

Attempting a transition from the wrong state (e.g. returning a `PENDING` order) throws `IllegalStateException`, mapped to `422 INVALID_STATE`.

## 🔌 API Reference

**Base path:** `/api`

| Symbol | Meaning |
|---|---|
| 🔓 | Public endpoint |
| 🔒 | Requires `Authorization: Bearer <jwt>` |

> **Tip:** Product catalog reads and product review reads are intentionally public so guests can browse before signing in.


## Users — `/api/users`

| Method | Path | Auth | Description |
| ------------------------- | ------------------ | -- | -------------------------------------------------------------------------------- |
| POST                      | `/register`        | 🔓 | Create account (BCrypt-hashes password, defaults role to USER)                   |
| POST                      | `/login`           | 🔓 | Authenticate; returns `{ token, user }`; enforces rate limit + lockout + CAPTCHA |
| GET                       | `/{id}`            | 🔒 | Fetch profile (self only — 403 otherwise)                                        |
| PUT                       | `/{id}`            | 🔒 | Update profile / password / address (self only)                                  |
| DELETE                    | `/{id}`            | 🔒 | Delete account (self only)                                                       |
| POST                      | `/forgot-password` | 🔓 | Check if an email exists                                                         |
| POST                      | `/verify-identity` | 🔓 | Verify email + phone match before reset                                          |
| POST                      | `/reset-password`  | 🔓 | Set new password after identity verification                                     |

## Products — `/api/products`

| Method | Path | Auth | Description |
| ------------------------- | ---------------------------------------- | -- | ---------------------------------------------- |
| GET                       | `/`                                      | 🔓 | List all products                              |
| GET                       | `/{id}`                                  | 🔓 | Product detail                                 |
| GET                       | `/featured`                              | 🔓 | Featured products                              |
| GET                       | `/category/{category}`                   | 🔓 | Filter by category                             |
| GET                       | `/category/{category}/subcategory/{sub}` | 🔓 | Filter by category + subcategory               |
| GET                       | `/subcategory/{sub}`                     | 🔓 | Filter by subcategory                          |
| GET                       | `/brand/{brand}`                         | 🔓 | Filter by brand                                |
| GET                       | `/search?q=&page=&size=`                 | 🔓 | Ranked full-text search (see design decisions) |
| GET                       | `/suggestions?q=`                        | 🔓 | Typeahead suggestions (max 6)                  |
| GET                       | `/price`                                 | 🔓 | Filter by price range                          |
| POST                      | `/`                                      | 🔒 | Create product (admin)                         |
| PUT                       | `/{id}`                                  | 🔒 | Update product (admin)                         |
| PATCH                     | `/{id}/featured`                         | 🔒 | Toggle featured flag (admin)                   |
| DELETE                    | `/{id}`                                  | 🔒 | Delete product (admin)                         |

## Orders — `/api/orders`

| Method | Path | Auth | Description |
| ------------------------- | ---------------------------- | -- | ------------------------------------------------------------------- |
| POST                      | `/checkout`                  | 🔒 | Atomic two-phase checkout (validate → deduct stock → persist order) |
| POST                      | `/`                          | 🔒 | Create order (legacy path, delegates to checkout)                   |
| GET                       | `/user/{userId}`             | 🔒 | Order history for a user                                            |
| GET                       | `/{orderId}`                 | 🔒 | Single order detail                                                 |
| PUT                       | `/{orderId}/cancel`          | 🔒 | Cancel a PENDING order                                              |
| POST                      | `/{orderId}/return`          | 🔒 | Initiate return (must be DELIVERED)                                 |
| PUT                       | `/{orderId}/status`          | 🔒 | Update status (admin)                                               |
| PUT                       | `/{orderId}/return/approve`  | 🔒 | Approve return request (admin)                                      |
| PUT                       | `/{orderId}/return/pickup`   | 🔒 | Schedule pickup (admin)                                             |
| PUT                       | `/{orderId}/return/picked`   | 🔒 | Mark picked up (admin)                                              |
| PUT                       | `/{orderId}/return/refund`   | 🔒 | Process refund (admin)                                              |
| PUT                       | `/{orderId}/return/complete` | 🔒 | Complete return lifecycle (admin)                                   |
| DELETE                    | `/{orderId}`                 | 🔒 | Hard delete (admin)                                                 |

## Cart — `/api/cart`

| Method | Path | Auth | Description |
| ------------------------- | ----------------------------- | -- | ------------------------------------------------------------------ |
| GET                       | `/{userId}`                   | 🔒 | Get or lazily create cart                                          |
| POST                      | `/{userId}/add`               | 🔒 | Add item (merges quantity if already present)                      |
| PUT                       | `/{userId}/items`             | 🔒 | Update item quantity (removes if ≤ 0)                              |
| DELETE                    | `/{userId}/items/{productId}` | 🔒 | Remove single item                                                 |
| DELETE                    | `/{userId}/clear`             | 🔒 | Empty the cart                                                     |
| POST                      | `/{userId}/sync`              | 🔒 | Merge a guest (localStorage) cart into the backend cart post-login |

## Reviews — `/api/reviews`

| Method | Path | Auth | Description |
| ------------------------- | ---------------------- | -- | -------------------------------------------------- |
| POST                      | `/`                    | 🔒 | Add review (recalculates product's average rating) |
| GET                       | `/product/{productId}` | 🔓 | Reviews for a product                              |
| PUT                       | `/{id}`                | 🔒 | Edit own review                                    |
| DELETE                    | `/{id}`                | 🔒 | Delete own review                                  |

## Wishlist — `/api/wishlist`

| Method | Path | Auth | Description |
| ------------------------- | ----------------------------------- | -- | ------------------------------- |
| GET                       | `/user/{userId}`                    | 🔒 | Get wishlist                    |
| POST                      | `/user/{userId}/add/{productId}`    | 🔒 | Add product                     |
| DELETE                    | `/user/{userId}/remove/{productId}` | 🔒 | Remove product                  |
| DELETE                    | `/user/{userId}/clear`              | 🔒 | Clear wishlist                  |
| POST                      | `/user/{userId}/sync`               | 🔒 | Merge guest wishlist post-login |

## Addresses — `/api/v1/addresses`

| Method | Path | Auth | Description |
| ------------------------- | --------------- | -- | -------------------------------------------------------------- |
| GET                       | `/`             | 🔒 | List saved addresses                                           |
| GET                       | `/{id}`         | 🔒 | Address detail                                                 |
| POST                      | `/`             | 🔒 | Create address                                                 |
| PUT                       | `/{id}`         | 🔒 | Update address                                                 |
| DELETE                    | `/{id}`         | 🔒 | Delete address                                                 |
| PATCH                     | `/{id}/default` | 🔒 | Set as default (service enforces exactly one default per user) |

## ⚠️ Error Handling

Every error returns a consistent JSON shape via `ApiErrorResponse`:

```json

`{   "success": false,   "code": "INSUFFICIENT_STOCK",   "message": "Only 3 units available, 5 requested.",   "timestamp": "2026-08-24T10:15:00",   "path": "/api/orders/checkout" }`

| Exception | HTTP status | Code |
| ----------------------------------------------------- | --- | ----------------------- |
| `InvalidCredentialsException`                         | 401 | `INVALID_CREDENTIALS`   |
| `AccountLockedException`                              | 423 | `ACCOUNT_LOCKED`        |
| `LoginTooSoonException`                               | 423 | `TOO_SOON`              |
| `CaptchaRequiredException`                            | 428 | `CAPTCHA_REQUIRED`      |
| `RateLimitExceededException`                          | 429 | `RATE_LIMIT_EXCEEDED`   |
| `EmailAlreadyExistsException`                         | 409 | `EMAIL_ALREADY_EXISTS`  |
| `PhoneAlreadyExistsException`                         | 409 | `PHONE_ALREADY_EXISTS`  |
| `InventoryConflictException`                          | 409 | `INVENTORY_CONFLICT`    |
| `ResourceNotFoundException`                           | 404 | `RESOURCE_NOT_FOUND`    |
| `IllegalArgumentException`                            | 400 | `BAD_REQUEST`           |
| `MethodArgumentNotValidException` (`@Valid` failures) | 422 | `VALIDATION_FAILED`     |
| `InsufficientStockException`                          | 422 | `INSUFFICIENT_STOCK`    |
| `MaxQuantityExceededException`                        | 422 | `MAX_QUANTITY_EXCEEDED` |
| `IllegalStateException` (bad order-status transition) | 422 | `INVALID_STATE`         |
| Anything else                                         | 500 | `INTERNAL_SERVER_ERROR` |

## ⚙️ Configuration

All values are set in `application.properties`, most overridable via environment variable:

| Property | Env var | Default | Purpose |
| -------------------------------------- | --------------------------------------------- | ---------------------------------------- | --------------------------------------------------- |
| `spring.data.mongodb.uri`              | `MONGODB_URI`                                 | `mongodb://localhost:27017/ecommerce_db` | Mongo connection                                    |
| `jwt.secret`                           | `JWT_SECRET`                                  | dev-only placeholder                     | HMAC signing key — must be overridden in production |
| `jwt.expiration-ms`                    | —                                             | `604800000` (7 days)                     | Token lifetime                                      |
| `server.port`                          | `PORT`                                        | `8080`                                   | HTTP port                                           |
| `security.login.max-failed-attempts`   | —                                             | `5`                                      | Attempts before hard lockout                        |
| `security.login.lock-duration-minutes` | —                                             | `15`                                     | Lockout duration (after the first offense)          |
| `security.login.progressive-delay.*`   | —                                             | `2s` base / `30s` cap                    | Exponential backoff between attempts                |
| `security.login.captcha.threshold`     | —                                             | `3`                                      | Failed attempts before CAPTCHA kicks in             |
| `turnstile.secret-key` / `site-key`    | `TURNSTILE_SECRET_KEY` / `TURNSTILE_SITE_KEY` | placeholders                             | Cloudflare Turnstile credentials                    |
| `app.frontend.url`                     | `APP_FRONTEND_URL`                            | `http://localhost:5173`                  | Allowed CORS origin                                 |

⚠️ `jwt.secret` and the Turnstile keys ship with placeholder defaults for local dev only — set real values via environment variables before deploying.

## 🚀 Getting Started

Prerequisites: Java 21, Maven, a running MongoDB instance (local or Atlas).

```bash

*`# 1. Set required environment variables (or rely on the localhost defaults for dev)`*export MONGODB_URI="mongodb://localhost:27017/ecommerce_db" export JWT_SECRET="replace-with-a-long-random-secret"   `*`# 2. Run`*` ./mvnw spring-boot:run`

On first run with an empty database, `DatabaseSeeder` (a `CommandLineRunner`) populates \~45 sample products across Electronics, Clothing, Books, Home, and Sports, plus a handful of demo orders in different statuses (`DELIVERED`, `PENDING`, `CANCELLED`) for a fixed demo user ID. It's idempotent — it checks `productRepository.count() > 0` / `orderRepository.existsByUserId(...)` before seeding, so it's a no-op on subsequent restarts.

The API is served at `http://localhost:8080/api/...` by default and expects the frontend at `http://localhost:5173` (configurable via `app.frontend.url`).

## 🛣️ Known Limitations & Roadmap

These are the main areas identified for a future production-scale iteration:

1. **Distributed rate limiting** — move Bucket4j state from the in-memory `ConcurrentHashMap` to Redis for multi-instance deployments.
2. **Production CAPTCHA configuration** — enable the actual Cloudflare Turnstile round-trip where real credentials are available.
3. **Stronger admin authorization** — enforce `ROLE_ADMIN` with method-level authorization such as `@PreAuthorize`.
4. **Scalable product search** — replace the capped regex candidate pool with a dedicated text index or MongoDB Atlas Search as catalog size grows.

- `RateLimiterService`'s Bucket4j buckets are held in an in-memory `ConcurrentHashMap` — fine for a single instance, but won't share state across multiple backend replicas. A Redis-backed bucket store would be the natural next step for horizontal scaling.
- Turnstile is currently configured with `turnstile.enabled=false` in the sample config — CAPTCHA enforcement itself (the threshold check in `UserService`) is always active, but the toggle exists to allow disabling the Cloudflare round-trip entirely in environments without real Turnstile keys.
- There's no admin-role gate at the controller level for the admin-only endpoints listed above (product mutation, order status/return admin actions) — they currently only require any authenticated JWT, not `ROLE_ADMIN` specifically. Worth tightening with a `@PreAuthorize("hasRole('ADMIN')")` pass.
- Product search ranks over a capped 300-document candidate pool fetched via regex — fine at current catalog size, but a dedicated text index (or
