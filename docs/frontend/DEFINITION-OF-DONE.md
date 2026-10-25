# Definition of Done (DoD) - Frontend Task Handoff Checklist

Every frontend feature task (T01–T26) must satisfy the following comprehensive Definition of Done before being submitted for review and merge.

---

## Screen & Feature Quality Checklist

### 1. Functional & UI Completeness
- [ ] Screen renders properly across all target breakpoints (Mobile `<768px`, Tablet `768-1024px`, Desktop `>1024px`).
- [ ] Supports all 4 standard visual states:
  - **Loading State**: Skeleton loaders or indicators matching layout dimensions.
  - **Empty State**: User-friendly empty state illustration and action button.
  - **Error State**: Non-blocking localized error banner/toast with retry action.
  - **Success State**: Clear confirmation feedback for mutating actions.
- [ ] Interactive leaf components are client components (`"use client"`); layout and data containers are Server Components.

### 2. Internationalization (i18n) & BIDI / RTL
- [ ] Zero hard-coded UI strings. All text sourced from `messages/ar.json` and `messages/en.json`.
- [ ] Arabic (**ar**, default) sets `<html lang="ar" dir="rtl">`.
- [ ] English (**en**) sets `<html lang="en" dir="ltr">`.
- [ ] Layout uses CSS Logical Properties exclusively (`margin-inline`, `padding-block`, `inset-inline`).
- [ ] Form error messages use backend `messageAr`/`messageEn` or localized translation keys.

### 3. Financial Data Integrity & Formatting (ADR-009)
- [ ] Money values are typed strictly as `string` with 2 decimal places (e.g., `"150.00"`).
- [ ] Zero JavaScript floating-point arithmetic (`+`, `-`, `parseFloat`) performed on prices.
- [ ] Displays final totals directly returned by API (`POST /checkout/quote` or `GET /carts/me`).
- [ ] Formatted using `Intl.NumberFormat` helpers (e.g., `"150.00 ج.م"`).

### 4. Accessibility & Performance
- [ ] Automated accessibility check via `@axe-core/playwright` passes with 0 violations.
- [ ] Semantic HTML tags (`<main>`, `<nav>`, `<header>`, `<footer>`, `<article>`, `<button>`).
- [ ] Full keyboard navigation support (visible focus rings, logical tab order, ESC key handlers).
- [ ] Screen First Load JS bundle meets budget (< 170 KB gzip).
- [ ] Images use Next.js `<Image />` with `alt` text, explicit aspect ratios, and lazy loading.

### 5. Code Quality & Boundaries
- [ ] TypeScript strict mode passes with 0 errors (`npm run typecheck`).
- [ ] ESLint passes with zero warnings (`npm run lint`).
- [ ] Feature isolation rule respected (no feature module imports another feature module directly).
- [ ] No secrets, tokens, passwords, or raw stack traces logged or rendered in UI.
- [ ] Open product decisions marked in code with `// OPEN-xx` and logged in `docs/frontend/open-decisions.md`.
- [ ] Contract gaps logged in `docs/frontend/contract-gaps.md`.

### 6. Testing & Backend Verification
- [ ] Unit & Component tests pass in Vitest.
- [ ] Playwright E2E tests pass for both **Arabic RTL** and **English LTR** projects.
- [ ] PR description includes mandatory **Backend Verification** section populated with evidence.
