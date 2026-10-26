# Frontend Handoff Verification, Security Facts & Local Run Check

## 1. Handoff Claims Verification Matrix

| # | Handoff Claim | Verdict | Code / Spec Evidence |
| :--- | :--- | :--- | :--- |
| **1** | Access token: JWT HS256, 15 mins (`expiresIn: 900`); Refresh token: 7 days; claim `typ` = access\|refresh; claim `role` present in access token. | **VERIFIED** | `JwtProperties.java` (`ACCESS_TOKEN_EXPIRATION_SECONDS = 900`, `REFRESH_TOKEN_EXPIRATION_SECONDS = 604800`), `JwtService.java` (`generateAccessToken` sets claim `role` and `typ: "access"`, `generateRefreshToken` sets `typ: "refresh"` using `HS256`). |
| **2** | Refresh returns `{ accessToken, tokenType, expiresIn }`; no token rotation. | **VERIFIED** | `AuthService.java` (returns `RefreshResponse` with new `accessToken`, `tokenType: "Bearer"`, `expiresIn: 900`). Refresh token can be reused multiple times until expiration (tested in R09). |
| **3** | Logout returns 204 No Content and does not revoke tokens on server. | **VERIFIED** | `AuthController.java` (`@PostMapping("/logout")` returns `ResponseEntity.noContent().build()`). Comments explicitly state: "Client-side logout signal only. Server is stateless and does not invalidate tokens." |
| **4** | Standard error response format `{ code, messageAr, messageEn, traceId, details }`. Error codes implemented: `VALIDATION_ERROR`, `EMAIL_OR_PHONE_REQUIRED`, `INVALID_CREDENTIALS`, `INVALID_TOKEN`, `FORBIDDEN`, `NOT_FOUND`, `EMAIL_ALREADY_REGISTERED`, `PHONE_ALREADY_REGISTERED`, `INTERNAL_ERROR`. | **VERIFIED** | `ApiErrorResponse.java` record definition and `GlobalExceptionHandler.java`. Standardized error responses across all exceptions. |
| **5** | Identical error response status and body for wrong email and wrong password during login. Dummy hash comparison prevents timing side-channel attacks. | **VERIFIED** | `AuthService.java` (`login` method uses `passwordEncoder.matches(rawPassword, DUMMY_BCRYPT_HASH)` on unknown user path and throws identical `invalidCredentials()` exception). Tested in R03 and R04. |
| **6** | Registration accepts email or phone, ignores any submitted `role` field (forces `CUSTOMER`), email lookup is case-insensitive. | **VERIFIED** | `AuthService.java` (`register` sets `user.setRole(UserRole.CUSTOMER)` regardless of input payload, uses `existsByEmailIgnoreCase`). Tested in R01 and R13. |
| **7** | Phone validation rules: optional `+`, 8 to 24 numeric digits (`^\+?[0-9]{8,24}$`). | **VERIFIED** | `AuthService.java` (`validatePhone` regex match). Note: `NEW-Q-01` flag notes potential future refinement for Egyptian (+20) formatting. |
| **8** | Addresses endpoints and fields (`recipientName`, `phone`, `countryCode`, `governorateId`, `cityId`, `district`, `street`, `building`, `apartment`, `postalCode`, `latitude`, `longitude`, `isDefault`). Default-address switching, 404 for addresses owned by another user. | **PARTIAL / SPEC-GAP** | `AddressController.java` & `AddressService.java` implement `GET`, `POST`, `GET /{id}`. **Missing endpoints in BOTH spec and backend**: `PUT /me/addresses/{id}`, `DELETE /me/addresses/{id}`, and `PATCH /me/addresses/{id}/default`. |
| **9** | Money fields are strings formatted with 2 decimal places (`^\d+\.\d{2}$`) and currency default EGP. Frontend never computes total; displays API response. | **VERIFIED** | `docs/api/openapi.yaml` (schemas type money as String with pattern `^\d+\.\d{2}$` and currency default `EGP`). DB stores as `numeric(14,2)`. |
| **10** | Pagination response shape `{ content, page, size, totalElements, totalPages }`, with 0-based page indexing. | **VERIFIED** | `docs/api/openapi.yaml` (`PagedResponse` schema definition). |
| **11** | `Idempotency-Key` header required on order creation (`POST /checkout/orders`). | **VERIFIED** | `docs/api/openapi.yaml:545` (`createCheckoutOrder` operation parameters declare `Idempotency-Key` header as `required: true`). |
| **12** | Guest cart/checkout token mechanism (`X-Guest-Token` header or `guestToken` body) and guest order tracking by token parameter (`GET /guest/orders/{token}`). | **CONTRADICTED / SECURITY-FLAG** | `docs/api/openapi.yaml:1255` defines security scheme header as `X-Guest-Cart-Token` (not `X-Guest-Token`). `GET /guest/orders/{token}` puts secret tracking token in URL path parameter (`docs/api/openapi.yaml:598`), which **violates Standing Rule 9** ("No secrets or tokens in URLs"). |

---

## 2. Missing-Contract Verification (T00b Audit Points 3a–3i)

### 3a. Payment Creation for Orders
- **Payment Creation Endpoint**: **ABSENT**. There is no `POST /orders/{orderId}/payments` operation in `docs/api/openapi.yaml` or backend code (`backend/src/main/java/com/classicstore/payment/package-info.java`).
- **Payment Flow**: Cash on Delivery (COD) is started via `POST /checkout/orders` with request body `paymentMethod: "COD"`, which creates the order directly with `paymentStatus: "PENDING"`. Online payments (CARD, FAWRY, VALU) have no endpoint to initiate a payment transaction or generate a gateway redirect URL.
- **Idempotency-Key**: Declared **ONLY** for `POST /checkout/orders` (`docs/api/openapi.yaml:545`). It is **NOT** declared for payment creation since no payment creation operation exists in `openapi.yaml`.

### 3b. Read Cart & Guest Cart Merge
- **Read Cart Endpoint (`GET /carts/{cartId}`)**: **ABSENT**. `docs/api/openapi.yaml:373-450` defines `POST /carts`, `PUT /carts/{cartId}/items/{sku}`, and `DELETE /carts/{cartId}/items/{sku}`, but no `GET` endpoint exists to fetch cart state.
- **Guest-Cart Merge on Login**: **ABSENT**. `LoginRequest` (`docs/api/openapi.yaml:180-230`) only accepts `{ email, password }`. No guest token header/body is accepted on login, nor is there a `POST /carts/merge` endpoint in `openapi.yaml` or `AuthService.java`.

### 3c. POST /carts Security, Guest Access & Token Details
- **Security in YAML**: `security: [ {}, { bearerAuth: [] } ]` (`docs/api/openapi.yaml:381-383`).
- **Unauthenticated Guest Access**: **VERIFIED YES**. The empty security requirement `{}` permits unauthenticated guests to create carts.
- **Token Delivery**: Returned in the JSON response **body** field `guestToken` of the `Cart` schema (`docs/api/openapi.yaml:1571`).
- **Exact Field Name**: `guestToken` (string | null).
- **Exact Header Name for Security Scheme**: `X-Guest-Cart-Token` (`docs/api/openapi.yaml:1255`).

### 3d. User Profile, User Orders & Staff List Endpoints
- **Read Current User Profile**: **ABSENT**. No `GET /me` or `GET /auth/me` endpoint exists (`docs/api/openapi.yaml:294` only contains `/me/addresses`).
- **List Current User Orders**: **ABSENT**. `docs/api/openapi.yaml:630` only defines `GET /orders/{orderId}` for single order retrieval; no `GET /orders` or `GET /me/orders` list endpoint exists.
- **List Orders for Staff**: **ABSENT**. No `GET /admin/orders` endpoint exists (`docs/api/openapi.yaml:708` only defines `POST /admin/orders/{orderId}/allocate`).
- **List Returns for Staff**: **ABSENT**. No `GET /admin/returns` endpoint exists (`docs/api/openapi.yaml:1022` only defines `POST /returns` and `POST /admin/refunds`).

### 3e. GET /products Query Parameters & ProductResponse Fields
- **Query Parameters (`docs/api/openapi.yaml:50-105`)**:
  - `q`: Search keyword query (string).
  - `categoryId`: Filter by category ID (string).
  - `brandId`: Filter by brand ID (string).
  - `minPrice`: Minimum price in EGP (`Money`).
  - `maxPrice`: Maximum price in EGP (`Money`).
  - `sort`: Sort field and direction (string, e.g. `price,asc` or `createdAt,desc`).
  - `page`: 0-based page index (integer, default 0, min 0).
  - `size`: Items per page (integer, default 20, min 1).
  - **Attribute Filters**: **ABSENT**. No query parameters for filtering by SKU attributes (e.g., color, size).
- **`ProductResponse` Schemas (`docs/api/openapi.yaml:1444-1520`)**:
  - `ProductSummary` (items in `GET /products` page): `id` (uuid), `slug` (string), `titleAr` (string), `titleEn` (string), `price` (`Money`), `currency` (string), `imageUrl` (string | null).
  - `ProductDetail` (`GET /products/{slug}`): `id` (uuid), `slug` (string), `titleAr` (string), `titleEn` (string), `descriptionAr` (string), `descriptionEn` (string), `price` (`Money`), `currency` (string), `skus` (array of objects with `sku`, `attributes` map, `inStock` boolean).
  - **Mismatches/Gaps**: `images` array (gallery) is **ABSENT**; `category` details are **ABSENT**; numerical `stock` count is **ABSENT** (only boolean `inStock` per SKU).

### 3f. Financial Schemas & CreateOrderRequest Validation
- **`Cart` Schema (`docs/api/openapi.yaml:1562`)**: `cartId`, `guestToken`, `items` (`sku`, `productTitleAr`, `productTitleEn`, `quantity`, `unitPrice`, `lineTotal`), `grandTotal`, `currency`. (`subtotal`, `discount`, `shippingFee`, `tax`, `reservationExpiresAt` are **ABSENT**).
- **`CheckoutQuote` Schema (`docs/api/openapi.yaml:1584`)**: `subtotal`, `shippingFee`, `discount`, `grandTotal`, `currency`. (`tax`, `reservationExpiresAt` are **ABSENT**).
- **`Order` Schema (`docs/api/openapi.yaml:1605`)**: `orderId`, `orderNumber`, `status`, `paymentStatus`, `currency`, `grandTotal`, `items`, `createdAt`. (`subtotal`, `discount`, `shippingFee`, `tax`, `reservationExpiresAt` are **ABSENT**).
- **`CreateOrderRequest` Schema (`docs/api/openapi.yaml:550-590`)**:
  - Required fields: `cartId` (UUID), `customer` (`name`, `phone`, `email`), `shippingAddress` (`AddressInput`), `paymentMethod`.
  - Optional fields: `couponCode` (string | null).
  - **CONFIRMED**: `CreateOrderRequest` contains **NO `grandTotal`**, **NO `status`**, and **NO `warehouseId`**.
  - Payment-method enum: `[COD, CARD, FAWRY]` (`docs/api/openapi.yaml:584`).

### 3g. Error Codes Diff (Spec vs Backend Code)
- **Backend Handled Error Codes (`GlobalExceptionHandler.java` & `BusinessException.java`)**:
  - `VALIDATION_ERROR` (HTTP 400)
  - `UNAUTHENTICATED` (HTTP 401)
  - `FORBIDDEN` (HTTP 403)
  - `NOT_FOUND` (HTTP 404)
  - `INTERNAL_ERROR` (HTTP 500)
  - `EMAIL_OR_PHONE_REQUIRED` (HTTP 422)
  - `EMAIL_ALREADY_REGISTERED` (HTTP 409)
  - `PHONE_ALREADY_REGISTERED` (HTTP 409)
  - `INVALID_INPUT` (HTTP 400 / HTTP 422)
  - `INVALID_CREDENTIALS` (HTTP 401)
  - `INVALID_TOKEN` (HTTP 401)
- **OpenAPI Spec Error Responses (`docs/api/openapi.yaml:1268, 1710`)**:
  - Standard HTTP error responses referencing `# /components/schemas/Error`.
  - Example codes used in spec: `"INVALID_INPUT"`, `"NO_ELIGIBLE_WAREHOUSE"` (lines 662, 674).
- **Diff**: The OpenAPI spec defines a generic string `code` field in `# /components/schemas/Error` with example `"INVALID_INPUT"`, but does **NOT** formally enumerate backend business error codes (`EMAIL_ALREADY_REGISTERED`, `PHONE_ALREADY_REGISTERED`, `INVALID_CREDENTIALS`, `INVALID_TOKEN`, `UNAUTHENTICATED`, `FORBIDDEN`, `INTERNAL_ERROR`) in an OpenAPI enum or schema definition.

### 3h. Security Flag: Token in URL Path for Guest Order Tracking
- **Spec Definition**: `GET /guest/orders/{token}` (`docs/api/openapi.yaml:598-615`).
- **Violation**: Standing Rule 9 states "No secrets, tokens, or passwords in code, logs, or URLs." URL path parameters are logged in browser history, HTTP proxy access logs, and server logs. Exposing guest tracking tokens in URL paths poses a security risk. The API should accept guest tokens via header (`X-Guest-Cart-Token` or `X-Guest-Order-Token`).

### 3i. COD Limit & Payment Configuration Fields
- **Verdict**: **ABSENT**.
- **Evidence**: Neither `docs/api/openapi.yaml` nor Spring Boot backend configuration exposes Cash on Delivery limits (max EGP threshold), COD surcharge fees, or payment gateway configurations to the client.

---

## 3. Security and Integration Facts

- **CORS Configuration**:
  - **Fact**: Spring Security (`SecurityConfig.java`) has **NO CORS configuration** configured.
  - **Impact**: Cross-origin browser requests from a web frontend (e.g., `http://localhost:3000`) directly to the backend API will be blocked by browser CORS policy.
  - **Frontend Recommendation**: Next.js app should use Next.js API route proxies (`/api/*`) or configure local Next.js rewrite proxies during development.

- **Security Filter Chain (`SecurityConfig.java`)**:
  - CSRF disabled (`AbstractHttpConfigurer::disable`).
  - Session creation policy: `SessionCreationPolicy.STATELESS`.
  - Filter order: `CorrelationIdFilter` -> `JwtAuthenticationFilter` -> `UsernamePasswordAuthenticationFilter`.
  - Access denied handler: Custom handler returning 403 status with `ApiErrorResponse` schema and MDC trace ID.

- **Route Authorization**:
  - Public routes (`.permitAll()`): `/actuator/health`, `/api/v1/test/**`, `/api/v1/auth/**`, plus unmatched routes like `/api/v1/products` and `/api/v1/guest/**`.
  - Protected routes (`.authenticated()`): `/api/v1/me/**`.
  - Admin routes (`.hasRole('ADMIN')`): `/api/v1/admin/**`.

- **Password Hashing & Credentials**:
  - `BCryptPasswordEncoder` used for user password hashing.
  - Dummy hash execution on missing user logins prevents timing side-channel user enumeration.

- **Rate Limiting**:
  - Not currently implemented in Spring Boot backend (marked with TODO in `AuthService.java`).

- **Token Storage Strategy**:
  - Backend is stateless and does not use HTTP cookies.
  - Access tokens (15 mins) and Refresh tokens (7 days) are returned in HTTP response body JSON.
  - Recommended Next.js strategy: Store refresh tokens in HttpOnly, SameSite=Strict cookies via Next.js server route, and keep access tokens in-memory in the Next.js client state.

---

## 4. Local Backend Run Check (READ-ONLY Execution)

Attempted to compile and run tests in `/backend` within the sandbox environment:

1. **Compilation Command**:
   ```bash
   cd backend && ./mvnw -q -DskipTests compile
   ```
   - **Result**: **SUCCESS** (Exit Code `0`). Code compiles cleanly with Java 21 and Maven Wrapper.

2. **Test Command**:
   ```bash
   cd backend && ./mvnw -q test
   ```
   - **Result**: **FAILED** (`ContainerLaunchException` / status 500 overlay mount error).
   - **Root Cause Analysis**: Backend integration tests (`ClassicStoreApplicationTests`, `FlywayMigrationIntegrationTest`, `IdentityIntegrationTests`) utilize Testcontainers to spin up PostgreSQL containers. In this sandbox environment, Docker-in-Docker overlay mount permissions fail (`failed to mount /tmp/containerd-mount... err: invalid argument`).
   - **Conclusion**: Local environment allows compiling backend Java code, but running Testcontainers-dependent tests requires a full Docker daemon environment with overlay support or a dedicated PostgreSQL instance.
