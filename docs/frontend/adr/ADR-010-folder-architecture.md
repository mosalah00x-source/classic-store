# ADR-010: Folder Architecture & Dependency Isolation Rules

* **Status**: ACCEPTED
* **Date**: 2026-03-30
* **Scope**: `/frontend` Source Tree Structure & Module Import Directives

---

## Context
As frontend codebases grow, loose component imports lead to spaghetti dependencies, circular references, and difficult refactoring. We need clear architecture boundaries enforced at build time.

---

## Decision
We adopt a **Clean, Feature-Based Architecture** organized into three distinct layers:

```
src/
├── app/         # Routing layer (Next.js App Router pages, layouts, route handlers)
├── features/    # Business domain modules (catalog, cart, checkout, auth, account, admin)
└── shared/      # Primitive UI components, utilities, design tokens, generic hooks
```

### Strict Import Dependency Rules:
1. **`app/`** can import from **`features/`** and **`shared/`**.
2. **`features/`** can import from **`shared/`** and internal sub-modules.
3. **FEATURE ISOLATION**: A feature module (`features/cart`) **MUST NOT** import directly from another feature module (`features/checkout`). Cross-feature communication occurs via URL state or shared state hooks.
4. **`shared/`** **MUST NOT** import from `features/` or `app/`.
5. **Data Layer Encapsulation**: Data fetching and API client logic reside inside per-feature API modules (`features/catalog/api/`) or `shared/lib/api/`. Components must not make raw un-encapsulated `fetch` calls.

---

## Consequences
* **Positive**:
  - Modular, highly testable code.
  - Prevents circular dependency chains.
  - Enforced automatically via ESLint `no-restricted-imports` boundaries.
* **Negative**:
  - Requires disciplined file placement when creating new components.

---

## Alternatives Considered
1. **Flat Component Tree (`src/components/*`)**: *Rejected*: Becomes unmaintainable as application scales across 26 planned tasks.
