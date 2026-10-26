# Shared API Layer (`src/shared/api`)

## Purpose

Contains global API clients, fetch wrappers, and shared DTO types used across multiple features or BFF routes.

## Allowed Imports (per ADR-010)

- May import from `src/shared/config/` and `src/shared/lib/`.
- Must NOT import from `src/features/` or `src/app/`.
