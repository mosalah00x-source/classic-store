# ADR-001: Repository Layout & Workspace Boundaries

* **Status**: ACCEPTED
* **Date**: 2026-03-30
* **Scope**: Project Directory Structure & CI Path Filtering

---

## Context
The repository `classic-store` hosts a Java Spring Boot backend on branch `main` and is introducing a modern Next.js storefront application. An existing HTML/CSS/JS visual prototype resides in `/classicstore-frontend`. To ensure clean separation between backend development, visual design references, and production frontend code, we need a clear repository layout strategy.

---

## Decision
1. **Directory Allocation**:
   - `/frontend`: Production Next.js 15 (App Router, TypeScript strict) application.
   - `/backend`: Spring Boot 3.4.2 Java backend (managed on `main`).
   - `/classicstore-frontend`: Read-only static UI prototype (reference only, untouched).
   - `/docs`: Shared specifications, OpenAPI contracts (`docs/api/openapi.yaml`), frontend ADRs (`docs/frontend/adr/`), and contract gap logs.

2. **Branching & CI Path Isolation**:
   - All frontend tasks branch from `front-end-Development` and target `front-end-Development`.
   - CI workflows will use GitHub Actions path filters (`paths: ['frontend/**', 'docs/frontend/**']`) to ensure frontend test runs do not trigger backend Java builds and vice-versa.

---

## Consequences
* **Positive**:
  - Eliminates merge conflicts between Java and TypeScript ecosystems.
  - Keeps build times fast by scoping CI triggers to affected directories.
  - Preserves the original visual prototype intact for design verification.
* **Negative**:
  - Frontend developers must pull contract changes from `docs/api/openapi.yaml` on `main` via merge commits.

---

## Alternatives Considered
1. **Separate Repositories**: Split frontend and backend into distinct GitHub repos. *Rejected*: Increases overhead for contract sync and cross-stack integration testing in early development phases.
2. **Monorepo Tools (Turborepo/Lerna)**: Place packages in `packages/*`. *Rejected*: Overkill for a two-tier application layout.
