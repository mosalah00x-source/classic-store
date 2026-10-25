# ADR-008: Quality Gates, Testing Strategy & Performance Budgets

* **Status**: ACCEPTED
* **Date**: 2026-03-30
* **Scope**: Code Standards, Testing Stack, Accessibility & Core Web Vitals Budgets

---

## Context
To prevent regression bugs, maintain high performance, and enforce accessibility across Arabic (RTL) and English (LTR) experiences, we need strict automated quality gates.

---

## Decision
1. **Automated Quality Tooling**:
   - **ESLint & Prettier**: Configured with strict import boundary rules (`import/order`, no feature-to-feature cross-imports).
   - **TypeScript**: Strict mode enabled (`"strict": true`, no `any`).
   - **Vitest & React Testing Library**: Unit and component tests.
   - **Playwright**: E2E test suites with dual-project configuration for **Arabic (RTL)** and **English (LTR)** viewports.
   - **axe-core**: Accessibility testing integrated into Playwright and component tests (`@axe-core/playwright`).

2. **Performance & Bundle Budgets**:
   - **First Load JS Budget**: Under **170 KB gzip** for public storefront routes.
   - **Core Web Vitals**: LCP < 2.5s, CLS < 0.1, INP < 200ms.
   - **Lighthouse CI Threshold**: Mobile Performance ≥ **90**, Accessibility ≥ **95**, Best Practices ≥ **95**.
   - Enforced in CI using `size-limit` and `@lhci/cli`.

---

## Consequences
* **Positive**:
  - Guarantees top-tier mobile performance and complete accessibility compliance.
  - Catches RTL visual bugs automatically in Playwright tests.
* **Negative**:
  - Requires initial setup overhead in CI pipelines.

---

## Alternatives Considered
1. **Manual Testing Only**: *Rejected*: High risk of regression and layout breakage in RTL/LTR edge cases.
