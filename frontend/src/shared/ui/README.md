# Shared UI Layer (`src/shared/ui`)

## Purpose

Contains generic, primitive UI components (e.g., Button, Input, Modal, Spinner, Typography) and reusable presentation elements without domain logic.

## Allowed Imports (per ADR-010)

- May import from `src/shared/lib/`, `src/shared/config/`, and `src/shared/i18n/`.
- Must NOT import from `src/features/`, `src/app/`, or `src/server/`.
