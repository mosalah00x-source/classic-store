# Internationalization (i18n) & Localization Guide

This guide explains how to handle multi-language support (Arabic default `ar`, English `en`), Right-to-Left (RTL) / Left-to-Right (LTR) layouts, entity field localization, message namespaces, and formatting utilities in ClassicStore.

---

## 1. Adding & Managing Messages

- Messages live in `src/shared/i18n/messages/ar.json` and `src/shared/i18n/messages/en.json`.
- Organize messages by namespace using the naming convention `feature.component.key`.
- Key parity and ICU placeholder matching are strictly enforced. Run `pnpm i18n:check` before submitting code changes.

### ICU & Plural Categories
Use ICU message syntax for placeholders and plurals.
Arabic requires six plural categories (`zero`, `one`, `two`, `few`, `many`, `other`), while English uses `one` and `other`.

Example (`common.cart.itemsCount`):
```json
// ar.json
"{count, plural, =0 {سلة التسوق فارغة} one {منتج واحد} two {منتجان} few {# منتجات} many {# منتجاً} other {# منتج}}"

// en.json
"{count, plural, =0 {Cart is empty} one {1 item} other {# items}}"
```

---

## 2. Server vs. Client Component Message Rules

- **Server Components**: Use `getTranslations('namespace')` or `useTranslations('namespace')`. They add zero bytes to the client JS bundle.
- **Client Components**: NEVER pass full message dictionaries to the client. Wrap client components in `NextIntlClientProvider` passing ONLY a picked subset of required namespaces:
  ```tsx
  const allMessages = await getMessages();
  const clientMessages = { home: allMessages.home };

  <NextIntlClientProvider messages={clientMessages}>
    <MyClientComponent />
  </NextIntlClientProvider>
  ```

---

## 3. Entity Localization (`pickLocalized`)

The backend returns paired localized fields for products and categories (e.g., `titleAr` / `titleEn`, `descriptionAr` / `descriptionEn`, `nameAr` / `nameEn`).

**Rule**: UI components MUST NOT branch on `locale` manually (e.g., `locale === 'ar' ? titleAr : titleEn`). They MUST use `pickLocalized`:

```ts
import { pickLocalized } from '@/shared/lib/format/pick-localized';

// Usage:
const title = pickLocalized(product, 'title', locale);
const description = pickLocalized(product, 'description', locale);
```

### Fallback Behavior:
1. Returns requested locale value if present and non-empty.
2. If primary locale value is empty/missing, falls back to alternate locale value.
3. If both are empty:
   - Development/Test: Throws an explicit error.
   - Production: Logs `console.error` (no PII) and returns an empty string fallback.

---

## 4. Formatting Utilities (`src/shared/lib/format/`)

All formatters are pure functions with Latin digits (`numberingSystem: "latn"`):

- **`formatMoney(amount: string, currency = 'EGP', locale = 'ar')`**:
  - `amount` MUST be a string with exactly 2 decimal places (`^\d+\.\d{2}$`, e.g., `"950.00"`).
  - Uses `Intl.NumberFormat` without `parseFloat`/`Number` arithmetic to prevent floating point inaccuracies.
- **`formatDiscountPercent(value, locale)`**: Formats discount percentages (e.g., `20%`).
- **`formatNumber(value, locale)`**: Formats numbers with Latin digits and locale grouping.
- **`formatDate(isoString, locale)`** & **`formatDateTime(isoString, locale)`**: Formats ISO-8601 strings in `Africa/Cairo` timezone using the Gregorian calendar.

---

## 5. Translating Backend Error Codes

Backend API responses contain an error `code` field. Use `errorMessageKey(code)` from `src/shared/i18n/error-messages.ts` to map API error codes to localized translation keys.

```ts
import { errorMessageKey } from '@/shared/i18n/error-messages';
import { useTranslations } from 'next-intl';

const t = useTranslations();
const messageKey = errorMessageKey(apiError.code);
const userFacingMessage = t(messageKey);
```

- Backend `messageAr`/`messageEn` fields are used ONLY as fallback for unknown error codes.
- Sensitive backend fields like `traceId` are NEVER shown to end users in production.

---

## 6. CSS Logical Properties & Linting

- All layout styles MUST use CSS logical properties (`margin-inline-start`, `padding-block-end`, `inset-inline-start`) to support RTL and LTR automatically.
- Forbidden: Physical `left`/`right` properties in CSS, unless justified with a comment.
- Run `pnpm lint:css` to verify logical CSS compliance via Stylelint.
