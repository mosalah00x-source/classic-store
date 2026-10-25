# Classic Store Repository Map & Infrastructure Audit

## Overview
This document provides a detailed mapping of the repository structure across branches (`main` and `front-end-Development`), confirming key infrastructure components, OpenAPI specifications, static prototypes, and GitHub Actions workflows.

---

## 1. Directory Tree (3 Levels Depth)

### Root / Directory Structure (`docs/t00-audit` merged with `main`)
```
.
├── .github/
│   └── workflows/
│       └── backend-ci.yml
├── backend/
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/classicstore/
│       │   └── resources/
│       └── test/
│           └── java/com/classicstore/
├── classicstore-frontend/
│   ├── README.md
│   ├── index.html
│   ├── assets/
│   │   ├── logo.png
│   │   ├── logo-original.jpeg
│   │   └── img/
│   ├── css/
│   │   ├── base.css
│   │   └── pages.css
│   └── js/ (Note: referenced in README)
├── Doc/
│   ├── Classic_Store_BRD_SRS_WBS_AR_v1.docx
│   ├── Classic_Store_SRS_Complete_AR_v1.docx
│   ├── DATA_DICTIONARY_AR.md
│   ├── DATA_DICTIONARY_DETAILED_AR.docx
│   ├── ERD_مخطط ERD.md
│   ├── README_AR_التوثيق العربي.md
│   ├── V7__integrity_and_reservation_hardening.sql
│   ├── classic_store_all.sql
│   └── mermaid-diagram_*.png
├── docs/
│   ├── api/
│   │   ├── .redocly.yaml
│   │   ├── identity-test-coverage.md
│   │   └── openapi.yaml
│   └── frontend/
│       └── [Deliverable documents written in T00]
└── README.md
```

### Branch Map & History Overview
- **`main`**: Contains Spring Boot backend (`/backend`), Flyway migrations (`V1` to `V7`), OpenAPI 3.1 specification (`docs/api/openapi.yaml`), identity unit/integration tests, and GitHub Actions CI workflow.
- **`front-end-Development`**: Contains static prototype UI (`/classicstore-frontend`). Merged with `main` at the start of Task T00.

---

## 2. Key Component Locations & Verification

| Component | Target / Claimed Path | Real Verified Path | Status |
| :--- | :--- | :--- | :--- |
| **Backend Application** | `/backend` | `/backend` | VERIFIED |
| **OpenAPI Specification** | `docs/api/openapi.yaml` | `docs/api/openapi.yaml` | VERIFIED |
| **Redocly Config** | `docs/api/.redocly.yaml` | `docs/api/.redocly.yaml` | VERIFIED |
| **Identity Coverage Matrix** | `docs/api/identity-test-coverage.md` | `docs/api/identity-test-coverage.md` | VERIFIED |
| **Static UI Prototype** | `/classicstore-frontend` | `/classicstore-frontend` | VERIFIED |
| **CI Workflows** | `.github/workflows/` | `.github/workflows/backend-ci.yml` | VERIFIED |

---

## 3. Static UI Prototype Analysis (`/classicstore-frontend`)

- **Role**: Visual and functional reference only. READ-ONLY. No code in this directory will be modified or moved.
- **Contents**:
  - `index.html`: Multi-view SPA structure using hash routing (`#home`, `#products`, `#product`, `#cart`, `#checkout`, `#done`).
  - `css/base.css`: CSS Design Tokens (`:root`), header, main page layout, product card styling, footer.
  - `css/pages.css`: Dark mode styles (`[data-theme]`), detail/cart/checkout view styling, LTR overrides, logo mask.
  - `assets/img/`: Image placeholders for hero, categories, products.
  - `assets/logo.png`: Transparent logo with CSS mask coloring.
  - `README.md`: Explains CSS design tokens, RTL/LTR i18n structure, dark mode, hash routing, and backend integration requirements.

---

## 4. GitHub Actions CI Workflows

Only one workflow exists in `.github/workflows/`:

### File: `.github/workflows/backend-ci.yml`
- **Name**: Backend CI
- **Triggers**:
  - `push` to paths `backend/**`, `docs/api/**`, `.github/workflows/**`
  - `pull_request` to paths `backend/**`, `docs/api/**`, `.github/workflows/**`
- **Concurrency**: Grouped by workflow and ref with `cancel-in-progress: true`.
- **Jobs**:
  1. **`api-lint`**:
     - Environment: `ubuntu-latest`, Node.js 20.
     - Command: `npx @redocly/cli lint docs/api/openapi.yaml --config docs/api/.redocly.yaml`
  2. **`backend-test`**:
     - Environment: `ubuntu-latest`, Java 21 (Temurin), Maven cache enabled.
     - Command: `./mvnw -B verify` in `/backend`.
  3. **`dependency-scan`**:
     - Environment: `ubuntu-latest`, Java 21 (Temurin).
     - Command: OWASP Dependency Check (`./mvnw -B org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=9.0`).
