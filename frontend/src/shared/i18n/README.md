# Shared i18n (`src/shared/i18n`)

## Purpose

Contains internationalization utilities, locale configurations, direction (RTL/LTR) helpers, and dictionary loaders.

## Allowed Imports (per ADR-010)

- May import from `src/shared/config/` and `src/shared/lib/`.
- Must NOT import from `src/features/` or `src/app/`.
