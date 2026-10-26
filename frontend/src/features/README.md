# Features Layer (`src/features`)

## Purpose

Contains feature-based business modules (e.g., catalog, cart, checkout, auth, account, admin). Each feature encapsulates its own components, API calls, hooks, and types.

## Allowed Imports (per ADR-010)

- May import from `src/shared/*` and internal sub-modules within the same feature.
- **FEATURE ISOLATION**: Feature modules MUST NOT import directly from other feature modules. Cross-feature communication occurs via URL parameters or shared state hooks in `src/shared/`.
- Must NOT import from `src/app/`.
