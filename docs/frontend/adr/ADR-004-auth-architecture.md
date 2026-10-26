# ADR-004: Authentication Architecture, Token Storage & BFF Strategy

* **Status**: ACCEPTED (Approved by owner on 2026-10-25)
* **Date**: 2026-10-25
* **Scope**: JWT Token Handling, Session Security & Next.js Backend-For-Frontend (BFF)

---

## Context
The Spring Boot backend issues Bearer JWT access tokens and refresh tokens upon login (`POST /auth/login`). Storing JWT tokens in browser `localStorage` exposes them to Cross-Site Scripting (XSS) attacks. Additionally, browser direct calls to the backend trigger CORS restrictions (GAP-01).

---

## Decision
We propose a **Backend-For-Frontend (BFF)** pattern utilizing Next.js Route Handlers (`/app/api/auth/*`):

1. **Token Storage**:
   - JWT Access and Refresh tokens are stored strictly in **`httpOnly`, `Secure`, `SameSite=Lax` cookies** set by Next.js Route Handlers.
   - Tokens are **never exposed to client-side JavaScript**.

2. **Guest Token Handling**:
   - Guest cart tokens (`X-Guest-Token`) are generated server-side or stored in an `httpOnly` cookie, forwarded automatically by Next.js API routes on guest operations.

3. **CORS Mitigation (GAP-01)**:
   - Client components fetch relative paths (`/api/*`), which Next.js forwards server-to-server to the Spring Boot backend (`http://localhost:8080/api/v1/*`), completely bypassing browser CORS enforcement.

4. **Refresh & Logout Flow**:
   - **Refresh**: When backend returns `401 Unauthorized`, the BFF route handler automatically attempts token refresh (`POST /auth/refresh`) using the refresh cookie before retrying the request.
   - **Logout**: Triggers server-side session invalidation and clears httpOnly cookies. Note: Current backend `POST /auth/logout` is client-side state drop; BFF route handles cookie clearance.

5. **CSRF Protection**:
   - Mutating BFF endpoints (`POST`, `PUT`, `DELETE`) require a custom `X-CSRF-Token` header validated against an encrypted cookie double-submit pattern.

---

## Consequences
* **Positive**:
  - Eliminates token theft via XSS.
  - Solves browser CORS issues transparently without modifying Spring Boot code during frontend phase.
* **Negative**:
  - Adds server-side processing overhead on Next.js edge/node runtime.
  - Requires explicit owner sign-off on cookie domain and session policies.

---

## Alternatives Considered
1. **LocalStorage Token Storage**: *Rejected*: High security risk; vulnerable to XSS script injection.
2. **Direct Backend Calls with CORS Configuration**: *Rejected*: Violates standing rule against modifying backend Java code from frontend branches.
