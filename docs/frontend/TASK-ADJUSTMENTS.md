# Frontend Task Adjustments (T01–T26) & Architecture Recommendations

## 1. Planned Tasks Audit & Execution Readiness Matrix (T01–T26)

Every planned task for the frontend roadmap is evaluated against the audited backend reality:
- **REAL**: Can run and integrate against the REAL Spring Boot backend immediately.
- **MOCK**: Depends on SPEC-ONLY backend endpoints or missing reference APIs; must execute against Mock Service Worker (MSW) or mock handlers initially.
- **BLOCKED**: Cannot proceed without an upstream product or backend blocker being resolved first.

| Task ID | Task Title / Description | Verdict | Rationale & Backend Dependencies |
| :--- | :--- | :--- | :--- |
| **T01** | Prototype preservation & ADRs | **REAL** | Frontend setup and documentation step; zero backend dependency. Static prototype `/classicstore-frontend` is read-only. |
| **T02** | Next.js scaffold & CI pipeline | **REAL** | Next.js app initialization in `/frontend`; setup GitHub Actions / Turbopack / Tailwind. |
| **T03** | i18n & RTL support | **REAL** | Pure frontend internationalization architecture (`next-intl` or custom i18n) supporting AR (RTL) & EN (LTR). |
| **T04** | Design tokens & base components | **REAL** | Map CSS tokens from `classicstore-frontend/css/base.css` into Tailwind CSS / React UI kit. |
| **T05** | Layout shell (Header, Footer, Nav) | **MOCK** | Shell structure can be built, but header mega menu category navigation requires mocking `GET /categories` (GAP-03). |
| **T06** | API layer generated from OpenAPI | **REAL** | Generate TypeScript client/types directly from `docs/api/openapi.yaml` using `@hey-api/openapi-ts` or `openapi-typescript`. |
| **T07** | Mock Service Worker (MSW) server | **REAL** | Essential infrastructure to simulate all SPEC-ONLY endpoints (Catalog, Cart, Checkout, Orders, Payments, Admin) seamlessly. |
| **T07b** | Local full-stack run environment | **REAL** | Provide a `docker-compose.yml` orchestrating PostgreSQL and the Spring Boot backend service with `SPRING_FLYWAY_ENABLED=true` injected via environment variable (no backend code or migration file modifications). Enables local full-stack development and testing against the real database schema. |
| **T08** | Auth flow (Register, Login, Refresh) | **REAL** | **Backend Auth is IMPLEMENTED**. Direct integration with `POST /auth/register`, `/login`, `/refresh`, `/logout`. |
| **T09** | UI states kit | **REAL** | Reusable components for Loading (skeleton), Error state, Empty state, and Success notifications across LTR/RTL. |
| **T10** | Quality gates & Playwright setup | **REAL** | ESLint, Prettier, TypeScript checks, and Playwright E2E framework integration. |
| **T11** | Home page | **MOCK** | Hero banners, featured products, and categories grid require SPEC-ONLY catalog endpoints (`GET /products`) and missing `GET /categories`. |
| **T12** | Product List / Catalog page | **MOCK** | `GET /products` search, filtering, and pagination are SPEC-ONLY on backend; requires MSW mock data. |
| **T13** | Product Detail page | **MOCK** | `GET /products/{slug}` is SPEC-ONLY on backend; requires MSW mock data. |
| **T14** | SEO & Metadata | **REAL** | Next.js dynamic metadata, Open Graph tags, canonical URLs, and structured JSON-LD schemas. |
| **T15** | Cart drawer & Cart page | **MOCK** | Cart API (`POST /carts`, `PUT /carts/...`, `DELETE /carts/...`) is SPEC-ONLY on backend; requires MSW mock handlers. |
| **T16** | Checkout page & Order placement | **MOCK** | `POST /checkout/quote` and `POST /checkout/orders` are SPEC-ONLY; governorate/city reference data missing (GAP-04). |
| **T17** | Payment handling | **BLOCKED** | **BLOCKED by GAP-11**. No payment creation endpoint (`POST /orders/{orderId}/payments`) exists in `openapi.yaml` or backend to initiate online payment sessions (Card, Fawry, Valu). |
| **T18** | Order Result & Guest Tracking | **MOCK** | `GET /guest/orders/{token}` is SPEC-ONLY on backend; requires MSW mock handlers. Note security flag GAP-17 for URL token parameter. |
| **T19** | Account & Address Management | **PARTIAL / REAL** | Address `GET`, `POST`, `GET /{id}` are **IMPLEMENTED** on backend. Address `PUT`, `DELETE`, `PATCH default` are missing in spec & backend (GAP-02) and must be mocked. |
| **T20** | Customer Order History | **BLOCKED** | **BLOCKED by GAP-13**. No customer order list endpoint (`GET /orders` or `GET /me/orders`) exists in `openapi.yaml` or backend to display order history. |
| **T21** | Returns Management | **MOCK** | `POST /returns` is SPEC-ONLY on backend. |
| **T22** | Admin Dashboard & Products | **MOCK** | `GET/POST /admin/products` are SPEC-ONLY on backend. |
| **T23** | Admin Inventory & Adjustments | **MOCK** | `GET /admin/inventory` and `POST /admin/inventory/adjustments` are SPEC-ONLY on backend. |
| **T24** | Admin Order Allocation | **MOCK** | `POST /admin/orders/{id}/allocate` is SPEC-ONLY on backend. |
| **T25** | Admin Fulfillment Management | **MOCK** | `PATCH /admin/fulfillments/{id}/status` is SPEC-ONLY on backend. |
| **T26** | Final Hardening & Performance | **REAL** | End-to-end integration polish, Lighthouse optimization, accessibility checks, performance audit. |

---

## 2. Proposed Frontend Directory Layout

The new Next.js 15 (App Router) application will be created in `/frontend` at the root of the repository:

```
.
├── backend/                  # Spring Boot 3.4.2 backend (Java 21)
├── classicstore-frontend/    # Static HTML/CSS/JS visual prototype (READ-ONLY)
├── docs/                     # Specifications, OpenAPI, audit deliverables
│   ├── api/
│   │   └── openapi.yaml
│   └── frontend/             # T00-T26 documentation deliverables
└── frontend/                 # [To be created in T02] Next.js App Router root
    ├── .env.example
    ├── next.config.ts
    ├── package.json
    ├── postcss.config.mjs
    ├── tailwind.config.ts
    ├── tsconfig.json
    ├── public/
    │   ├── assets/
    │   └── favicon.ico
    ├── src/
    │   ├── app/              # Next.js App Router (RTL/LTR locale routes)
    │   │   ├── [locale]/
    │   │   │   ├── (auth)/
    │   │   │   ├── (main)/
    │   │   │   ├── admin/
    │   │   │   └── layout.tsx
    │   │   └── api/          # Next.js API route proxies (prevents CORS issues)
    │   ├── components/       # Clean feature-based components
    │   │   ├── ui/           # Base design tokens & primitive UI components
    │   │   ├── auth/
    │   │   ├── cart/
    │   │   ├── checkout/
    │   │   ├── catalog/
    │   │   └── layout/
    │   ├── lib/              # Generated API client, fetchers, MSW handlers
    │   │   ├── api/
    │   │   └── mocks/        # MSW mock handlers & fixtures
    │   ├── store/            # Lightweight state management (Zustand/Jotai)
    │   └── types/            # TypeScript domain models
    └── tests/                # Playwright E2E and component tests
```

### Repo Conflicts Check
- No existing directory conflicts with `/frontend`.
- `/classicstore-frontend` is completely separate and untouched.

---

## 3. Top Risks & Blockers

1. **Risk 1: Backend CORS Missing (GAP-01)**
   - *Impact*: Direct client-side browser fetch calls to the Spring Boot backend (`http://localhost:8080`) fail due to CORS.
   - *Mitigation*: Configure Next.js rewrite proxy in `next.config.ts` (`/api/v1/:path*` -> `http://localhost:8080/api/v1/:path*`) during development.

2. **Risk 2: Heavy Reliance on MSW due to SPEC-ONLY Domains (GAP-07)**
   - *Impact*: 80% of frontend screens (Catalog, Cart, Checkout, Orders, Payments, Admin) rely on mock data because Java backend controllers do not exist yet.
   - *Mitigation*: Ensure MSW handlers strictly implement schemas from `docs/api/openapi.yaml` so transition to real backend requires zero frontend code rewrite.

3. **Risk 3: Incomplete Address Mutation Endpoints (GAP-02)**
   - *Impact*: Real backend auth test works, but address edit/delete/default switching endpoints return 404/405.
   - *Mitigation*: Mock `PUT`, `DELETE`, and `PATCH default` address operations in MSW until backend exposes those controller endpoints.

4. **Risk 4: Missing Category Tree & Governorate Reference APIs (GAP-03 & GAP-04)**
   - *Impact*: Frontend cannot dynamically render the header mega menu or checkout dropdowns from the API.
   - *Mitigation*: Create static JSON reference fixtures for Egyptian governorates/cities and catalog categories in `src/lib/mocks/`.

5. **Risk 5: Testcontainers Run Failure in Local/Sandbox Runner**
   - *Impact*: `./mvnw test` fails in Docker-in-Docker sandbox environments.
   - *Mitigation*: Rely on OpenAPI spec and MSW contract testing for frontend verification; use Spring Boot compile verification (`./mvnw compile`) for backend check.

6. **Blocker 1: Task T17 Blocked by Missing Payment Creation Endpoint (GAP-11)**
   - *Impact*: Online payment gateway flows cannot be implemented or tested end-to-end.
   - *Mitigation*: Requires backend BR-01 (`POST /orders/{orderId}/payments`).

7. **Blocker 2: Task T20 Blocked by Missing Customer Order History Endpoint (GAP-13)**
   - *Impact*: Customer account order history page cannot fetch list of user orders.
   - *Mitigation*: Requires backend BR-05 (`GET /orders`).
