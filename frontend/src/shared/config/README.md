# Shared Configuration (`src/shared/config`)

## Purpose

Contains environment variable validation schemas, application configuration, and runtime constants.

## Allowed Imports (per ADR-010)

- Standalone layer. Does not import from `src/app/`, `src/features/`, or other `src/shared/` subfolders except external/stdlib libraries.
