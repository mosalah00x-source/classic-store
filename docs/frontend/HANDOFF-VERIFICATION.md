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
| **8** | Addresses endpoints and fields (`recipientName`, `phone`, `countryCode`, `governorateId`, `cityId`, `district`, `street`, `building`, `apartment`, `postalCode`, `latitude`, `longitude`, `isDefault`). Default-address switching, 404 for addresses owned by another user. | **PARTIAL** | `AddressController.java` & `AddressService.java` implement `GET`, `POST`, `GET /{id}`, default address unsetting, and 404 on unowned addresses. **Missing endpoints**: `PUT /me/addresses/{id}`, `DELETE /me/addresses/{id}`, and `PATCH /me/addresses/{id}/default`. |
| **9** | Money fields are strings formatted with 2 decimal places (`^\d+\.\d{2}$`) and currency default EGP. Frontend never computes total; displays API response. | **VERIFIED** | `docs/api/openapi.yaml` (schemas type money as String with pattern `^\d+\.\d{2}$` and currency default `EGP`). DB stores as `numeric(14,2)`. |
| **10** | Pagination response shape `{ content, page, size, totalElements, totalPages }`, with 0-based page indexing. | **VERIFIED** | `docs/api/openapi.yaml` (`PagedResponse` schema definition). |
| **11** | `Idempotency-Key` header required on order creation (`POST /checkout/orders`). | **VERIFIED** | `docs/api/openapi.yaml` (`createCheckoutOrder` operation parameters declare `Idempotency-Key` as `required: true`). |
| **12** | Guest cart/checkout token mechanism (`X-Guest-Token` header or `guestToken` body) and guest order tracking by token parameter (`GET /guest/orders/{token}`). | **VERIFIED** | `docs/api/openapi.yaml` (`guestCartToken` security scheme, `X-Guest-Token` header, and `GET /guest/orders/{token}`). DB stores `guest_token_hash`. |

---

## 2. Security and Integration Facts

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

## 3. Local Backend Run Check (READ-ONLY Execution)

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
