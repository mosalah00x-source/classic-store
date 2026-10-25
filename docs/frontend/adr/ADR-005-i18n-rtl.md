# ADR-005: Internationalization (i18n), Localization & RTL Support

* **Status**: ACCEPTED
* **Date**: 2026-03-30
* **Scope**: Multi-language Routing, Arabic/English Translations & Directional Layouts

---

## Context
ClassicStore targets Egyptian and MENA consumers, making Arabic (**ar**, default) and English (**en**) essential. The UI must render Right-To-Left (RTL) for Arabic and Left-To-Right (LTR) for English without layout breakage or flickering.

---

## Decision
1. **Routing & Library**:
   - Use `next-intl` integrated with App Router locale subpaths (`/ar/products`, `/en/products`).
   - Default locale is **`ar`**.

2. **Document Direction & Layout**:
   - Server-side rendering sets `<html lang="ar" dir="rtl">` or `<html lang="en" dir="ltr">` based on URL locale parameter.
   - All component styling MUST use **CSS Logical Properties** (e.g., `margin-inline-start`, `padding-block-end`, `inset-inline-start`) instead of directional properties (`margin-left`, `left`).

3. **Translations & Error Mapping**:
   - No hard-coded UI strings. All labels, buttons, and titles are sourced from `messages/ar.json` and `messages/en.json`.
   - Backend API error codes map to localized user messages using `messageAr` / `messageEn` fields or client translation fallback dictionaries.

---

## Consequences
* **Positive**:
  - Zero layout flickering during language switching.
  - Native RTL/LTR support with zero CSS duplication.
  - Full SEO localization with dynamic `hreflang` metadata.
* **Negative**:
  - Developers must be disciplined in using CSS logical properties rather than traditional `left`/`right`.

---

## Alternatives Considered
1. **Client-side Translation Dictionary (Prototype Approach)**: *Rejected*: Flaky performance, search engine indexing failures, and text flicker on load.
