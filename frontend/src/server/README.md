# Server-Only Modules (`src/server`)

## Purpose

Contains server-exclusive logic including BFF route handlers, cookie encryption/session management, and backend-facing proxy functions.

## Security Protection

All files in `src/server/` MUST import the `'server-only'` package at the top to ensure build-time enforcement preventing server-only code from being bundled into client-side JavaScript.

## Allowed Imports (per ADR-010)

- May import from `src/shared/*`.
- Cannot be imported by any Client Components (`'use client'`).
