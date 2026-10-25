# ClassicStore Prototype Reference & Porting Guide

This document analyzes the static UI prototype in `/classicstore-frontend` (`index.html`, `css/base.css`, `css/pages.css`) and defines how visual elements, design tokens, and user interactions map into the target Next.js App Router application (`/frontend`).

---

## 1. Prototype View Mapping & Backend Readiness

Below is the mapping from prototype DOM views (`index.html`) to target Next.js routes, components, required data, corresponding OpenAPI operations, and backend execution status (`REAL`, `MOCK`, or `MISSING`).

| Prototype View ID | Target Next.js Route | Main Components | Data Needed | OpenAPI Operation / Path | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `#home` | `/[locale]` | `HeroBanner`, `CategoryGrid`, `FeaturedCarousel`, `PromoSection`, `TrustBadges` | Banners, Categories, Featured Products | `GET /products` (SPEC-ONLY), `GET /categories` (MISSING - GAP-03) | **MOCK** |
| `#products` | `/[locale]/products` | `ProductGrid`, `CategoryFilter`, `SortDropdown`, `Pagination` | Paginated product list, category metadata, filters | `GET /products` (SPEC-ONLY - GAP-07) | **MOCK** |
| `#product` | `/[locale]/products/[slug]` | `ProductGallery`, `ProductInfo`, `PriceDisplay`, `VariantSelector`, `AddToCartForm` | Detailed product specs, images, variants, stock | `GET /products/{slug}` (SPEC-ONLY - GAP-07) | **MOCK** |
| `#cart` | `/[locale]/cart` | `CartItemList`, `CartQuantityControl`, `CartSummary`, `PromoCodeForm` | Active cart items, price subtotal, estimated total | `GET /carts/me`, `POST /carts/me/items` (SPEC-ONLY - GAP-07) | **MOCK** |
| `#checkout` | `/[locale]/checkout` | `ContactSection`, `ShippingAddressForm`, `PaymentMethodSelector`, `CheckoutSummary` | Guest/user profile, governorates/cities, quote, cart | `POST /checkout/quote`, `POST /checkout/orders` (SPEC-ONLY), reference APIs (MISSING - GAP-04) | **MOCK** |
| `#done` | `/[locale]/orders/confirmation` | `OrderSuccessCard`, `OrderDetailsSummary`, `ContinueShoppingButton` | Placed order status, order number, tracking token | `GET /orders/{id}` or `GET /guest/orders/{token}` (SPEC-ONLY - GAP-07) | **MOCK** |

---

## 2. Design Tokens Extracted from CSS

Design tokens are extracted directly from `classicstore-frontend/css/base.css` and `css/pages.css`:

### Color Palette
- **Background (`--bg`)**: `#fafafa` (Light) / `#0d0d0d` (Dark)
- **Surface (`--surf`)**: `#ffffff` (Light) / `#171717` (Dark)
- **Primary Text (`--text`)**: `#1d1d1f` (Light) / `#f5f5f7` (Dark)
- **Muted Text (`--muted`)**: `#666666` (Light) / `#a1a1a6` (Dark)
- **Brand Accent Primary (`--org`)**: `#e0592a`
- **Brand Accent Secondary / Sale (`--red`)**: `#e30613`
- **Success / New Badge (`--green`)**: `#2e8b4f`
- **Button Surface (`--btn`)**: `#0e0e0e` (Light) / `#ffffff` (Dark)
- **Button Text (`--btnt`)**: `#ffffff` (Light) / `#0e0e0e` (Dark)
- **Border Default (`--bd`)**: `#dcdcdc`
- **Border Secondary (`--bd2`)**: `#ececec`
- **Disabled / Neutral Gray (`--gray`)**: `#9a9a9a`
- **Image Placeholder Surface (`--cimg`)**: `#f0f0f0`

### Typography
- **Primary Font Family**: `"Cairo", "Segoe UI", Tahoma, sans-serif`
- **Base Line Height**: `1.5`
- **Font Sizes**:
  - `xs`: `10px` / `12px` (badges, subtext)
  - `sm`: `13px` / `14px` (navigation, secondary labels)
  - `md`: `15px` / `17px` (body text, product titles, section headers)
  - `lg`: `24px` / `28px` (section titles, page headings)

### Layout & Spacing
- **Container Padding (`--pad`)**: `clamp(16px, 3.7vw, 70px)`
- **Border Radii**:
  - Small (`input`, `button`): `8px`
  - Medium (`card`, `dropdown`): `16px` / `18px`
  - Pill / Badge (`tag`): `14px` / `50%`
- **Z-Index Scale**:
  - Sticky Header: `40`
  - Mega Menu / Overlays: `50`
  - Toasts / Modals: `100`

### Shadows & Breakpoints
- **Box Shadows**: `0 8px 30px rgba(0, 0, 0, 0.12)` (Mega menu & elevated cards)
- **Breakpoints**:
  - Mobile: `< 768px`
  - Tablet: `768px - 1024px`
  - Desktop: `> 1024px`

---

## 3. Interactive Behaviors Analysis

### Behaviors to Port
1. **Mega Menu**: Desktop category menu with hover/focus disclosure. Must be accessible via keyboard and screen readers.
2. **Horizontal Drag/Touch Carousel**: Product card carousels with smooth touch scrolling and snap alignment (`scroll-snap-align: start`).
3. **Theme Switcher**: Dark/Light mode toggle without FOUC (Flash of Unstyled Content), managed via server-rendered `data-theme` attribute on `<html>`.
4. **Locale Switcher**: Seamless language toggle between Arabic (AR, default RTL) and English (EN, LTR), updating URL prefix (`/ar` / `/en`) and document direction (`dir="rtl"` / `dir="ltr"`).
5. **Cart Badge Indicator**: Reactive item count badge on header cart icon.

### Behaviors to DROP
1. **Demo Floating Navigation Bar (`#dn`)**: Prototype bottom bar used to switch DOM views (`#home`, `#products`, etc.). Replaced by Next.js App Router routing.
2. **Client-side Dictionary Translation**: In-browser string replacement (`js/i18n.js`). Replaced by `next-intl` server-side dictionary loading.
3. **Client-side Price & Order Total Calculations**: Prototype JS code calculating subtotals, discounts, and totals using JavaScript floating-point numbers.
4. **Hard-coded In-Memory Product List**: Static mock product arrays in JS. Replaced by API client and MSW mock handlers.
5. **Base64 / Inline Image Artifacts**: Replaced by Next.js `<Image />` component with lazy loading, WebP/AVIF optimization, and responsive `srcset`.

---

## 4. Prototype vs API Contract Conflicts

1. **Price Precision and Formatting**:
   - *Prototype*: Formats prices as standard floats, sometimes showing 3 decimals or raw floats (e.g., `712.5`).
   - *API*: All monetary amounts in `openapi.yaml` are strict **Strings with 2 decimal places** (e.g., `"712.50"`). Currency is implicit EGP. The frontend must display string values directly without JS float arithmetic.

2. **Order & Cart Totals**:
   - *Prototype*: Client JS computes cart subtotal, discount, shipping, and total.
   - *API*: The server calculates all totals (`POST /checkout/quote`). The frontend must render totals returned by the server quote response.

3. **Governorates & Cities Form Input**:
   - *Prototype*: Hard-coded `<select>` HTML element with free-text string values (`القاهرة`, `الجيزة`, `الإسكندرية`).
   - *API*: `openapi.yaml` requires discrete integer IDs (`governorateId`, `cityId`). Missing reference API endpoints (`GAP-04`) will be backed by MSW handlers.

4. **Wishlist / Favorites**:
   - *Prototype*: Client-side heart button toggle (`.fav`).
   - *API*: No wishlist endpoints exist in `openapi.yaml` (`GAP-05`). Wishlist state will persist in local browser storage (`localStorage`) until backend support is added.
