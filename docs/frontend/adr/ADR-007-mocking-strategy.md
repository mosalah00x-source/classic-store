# ADR-007: Mocking Strategy & API Schema Contract Validation

* **Status**: ACCEPTED
* **Date**: 2026-03-30
* **Scope**: MSW Setup, Test Fixtures & Missing Endpoint Simulation

---

## Context
80% of backend domain controllers (Catalog, Cart, Checkout, Orders, Payments, Admin) are currently SPEC-ONLY in `openapi.yaml` and not implemented in Java code (`GAP-07`). Furthermore, reference APIs for categories and governorates are missing completely (`GAP-03`, `GAP-04`). Frontend development must proceed without waiting for backend implementation.

---

## Decision
1. **Mock Service Worker (MSW 2.x)**:
   - Use MSW in both browser and Node environments (`msw/browser` and `msw/node`).
   - Mock handler responses are strictly typed using generated OpenAPI TypeScript interfaces.

2. **Schema Contract Testing**:
   - Automated contract test suite validates every MSW mock response payload against `docs/api/openapi.yaml` JSON schema using `ajv` validator.

3. **Missing Endpoint Strategy (`mocks/planned/`)**:
   - Endpoints missing from the OpenAPI specification (Categories Tree `GAP-03`, Governorates/Cities `GAP-04`, Wishlist `GAP-05`) are located in `src/lib/mocks/planned/`.
   - Handlers are explicitly commented with `// TODO(BR-xx): Awaiting OpenAPI schema update` and use hand-written TypeScript types.

4. **Environment Toggle**:
   - Controlled via environment variable `NEXT_PUBLIC_API_MOCKING=true|false`. Switching to real API requires zero component refactoring.

---

## Consequences
* **Positive**:
  - Allows full frontend feature development (T02–T26) independently of backend progress.
  - Contract validation tests guarantee seamless zero-friction backend integration when Java APIs launch.
* **Negative**:
  - Requires ongoing maintenance of MSW mock fixtures.

---

## Alternatives Considered
1. **Static JSON Mock Imports**: *Rejected*: Does not test network handling, status codes, or middleware behaviors.
