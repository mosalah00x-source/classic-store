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
   - Use `next-intl` (exact pinned `4.14.9`) integrated with App Router locale subpaths (`/ar`, `/en`).
   - Default locale is **`ar`**.
   - `localePrefix`: `"always"` (`/ar/...` and `/en/...`).
   - `localeDetection`: `false` (root `/` always redirects to `/ar`; explicit locale choice saved in cookie).

2. **Document Direction & Layout**:
   - Server-side rendering sets `<html lang="ar" dir="rtl">` or `<html lang="en" dir="ltr">` in raw HTML based on URL locale parameter.
   - All component styling MUST use **CSS Logical Properties** (e.g., `margin-inline-start`, `padding-block-end`, `inset-inline-start`) instead of directional properties (`margin-left`, `left`), enforced via `stylelint-use-logical-spec`.

3. **Translations & Error Mapping**:
   - No hard-coded UI strings in components. Messages live in `src/shared/i18n/messages/ar.json` and `en.json`.
   - Backend API error codes map to localized UI keys via `errorMessageKey(code)`.
   - Missing key policy: throws in dev/test, logs `console.error` (no PII) and returns fallback key in production.

4. **Formatting & Digits**:
   - `formatMoney(amount)` accepts strictly string values with 2 decimals (`^\d+\.\d{2}$`). Never uses `parseFloat`/`Number` arithmetic.
   - Digits: Latin digits (`numberingSystem: "latn"`) are used for both Arabic and English locales (OPEN-01 decision).
   - Dates/Times use `Africa/Cairo` timezone and Gregorian calendar.

5. **Entity Localization Helper**:
   - Entity paired fields (e.g., `titleAr`/`titleEn`, `descriptionAr`/`descriptionEn`) are retrieved using `pickLocalized(entity, baseKey, locale)`. Components must not branch on locale directly.

6. **Message Payload Boundary & Key Parity**:
   - Server components use `getTranslations()`. Client components receive ONLY picked namespace subsets via `NextIntlClientProvider`. Full message files must never reach the client bundle.
   - Key parity and ICU placeholder matching between `ar.json` and `en.json` are verified via `pnpm i18n:check`.
