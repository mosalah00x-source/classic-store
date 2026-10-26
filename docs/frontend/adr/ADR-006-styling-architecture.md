# ADR-006: Styling Architecture & Design Token System

* **Status**: ACCEPTED
* **Date**: 2026-03-30
* **Scope**: CSS Token Management, Dark Mode, Component Scoping & Performance

---

## Context
The prototype visual design is defined using CSS custom properties (`var(--bg)`, `var(--org)`, etc.) in `classicstore-frontend/css/base.css`. We need a modular, zero-runtime styling solution that maintains brand tokens, supports light/dark mode without FOUC, and keeps JavaScript bundle sizes small.

---

## Decision
1. **CSS Variables + CSS Modules**:
   - Design tokens are declared as global CSS custom properties in `src/styles/tokens.css` (mapped 1:1 from prototype design tokens).
   - Component styles use **CSS Modules** (`*.module.css`) consuming standard CSS custom properties and CSS logical properties.

2. **Dark Mode Execution**:
   - Theme (`light` / `dark`) is controlled via `data-theme` attribute on `<html>`.
   - Theme preference is stored in a cookie and set during server-side HTML rendering, completely preventing light/dark theme flash (FOUC).

3. **No Heavy UI Component Libraries**:
   - Heavy runtime CSS-in-JS libraries (e.g., MUI, Emotion, Styled Components) are prohibited to minimize JavaScript execution overhead and bundle footprint.

---

## Consequences
* **Positive**:
  - Zero CSS runtime overhead; ultra-fast page rendering.
  - Precise alignment with visual prototype CSS definitions.
  - Keeps storefront First Load JS well under the 170 KB limit.
* **Negative**:
  - Custom UI primitives must be built using accessible headless patterns.

---

## Alternatives Considered
1. **Tailwind CSS**: *Considered*: Excellent utility framework, but raw CSS Modules with logical properties match the prototype's pre-existing CSS structure more directly and reduce configuration complexity.
2. **MUI / Ant Design**: *Rejected*: Massive JS bundle footprint (>300 KB) violating performance budgets.
