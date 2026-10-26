# App Router Layer (`src/app`)

## Purpose

Contains Next.js App Router routes, layouts, pages, loading states, error boundaries, and BFF API route handlers (`src/app/api/*`).

## Allowed Imports (per ADR-010)

- May import from `src/features/`, `src/shared/*`, and `src/server/*`.
- Must not contain business domain state logic or raw API fetch implementations (delegate to `src/features/` and `src/shared/`).
