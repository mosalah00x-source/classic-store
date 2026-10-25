# ADR-002: Rendering and Caching Strategy

* **Status**: ACCEPTED
* **Date**: 2026-03-30
* **Scope**: Next.js App Router, Component Architecture & Caching Policies

---

## Context
E-commerce applications require high Core Web Vitals (LCP < 2.5s, CLS < 0.1) for public storefront pages (Home, Catalog, Product Detail) while delivering secure, dynamic, real-time interactivity for personalized user flows (Cart, Checkout, Account, Admin).

---

## Decision
We adopt **Next.js 15 App Router** with TypeScript in strict mode, adhering to the following rules:

1. **Server Components by Default**:
   - Every layout and page component is a React Server Component (RSC) unless interactivity (event handlers, react state) requires client JS.
   - Client components (`"use client"`) are pushed to interactive leaf nodes (e.g., `AddToCartButton`, `ThemeToggle`).

2. **Route-Specific Rendering Policies**:
   - **Storefront / Catalog Pages** (`/`, `/products`, `/products/[slug]`): Incremental Static Regeneration (ISR) / Server-Side Generation (SSG) with tag-based revalidation (`next: { tags: ['products'] }`).
   - **Cart & Checkout Pages** (`/cart`, `/checkout`): Fully dynamic (`export const dynamic = 'force-dynamic'`), `no-store` cache policy, and marked `noindex` for search engines.
   - **Account & Admin Pages** (`/account`, `/admin`): Fully dynamic client-authenticated pages.

---

## Consequences
* **Positive**:
  - Maximum initial load performance and optimal SEO for public product pages.
  - Minimal client JavaScript bundle sizes (under 170 KB target).
* **Negative**:
  - Developers must carefully manage Server vs Client component boundaries.

---

## Alternatives Considered
1. **Pure Single Page Application (SPA / Vite)**: *Rejected*: Poor initial LCP, sub-optimal SEO, and lack of server-side data fetching security.
2. **Full Dynamic SSR for All Pages**: *Rejected*: Unnecessary server latency and database load for static product catalog views.
