# ADR-009: Money, Currency & Numeric Value Handling

* **Status**: ACCEPTED
* **Date**: 2026-03-30
* **Scope**: Precision, Price Formatting & Financial Data Integrity

---

## Context
Handling financial amounts using IEEE 754 floating-point numbers in JavaScript (`number` type) causes rounding errors (e.g., `0.1 + 0.2 = 0.30000000000000004`). In e-commerce, price discrepancies erode user trust and cause payment gateway verification failures.

---

## Decision
1. **Monetary Amounts as Strings**:
   - All monetary values (`unitPrice`, `subtotal`, `taxTotal`, `total`) are strictly **Strings formatted with 2 decimal places** (e.g., `"150.00"`, `"1250.50"`).
   - TypeScript interfaces type price fields strictly as `string`, never `number`.

2. **No Frontend Financial Computations**:
   - The frontend NEVER performs financial arithmetic (`+`, `-`, `*`, `/`) or `parseFloat()` on monetary amounts to compute cart or order totals.
   - All final totals, subtotals, shipping fees, and discounts MUST be computed server-side and rendered directly from API responses (`POST /checkout/quote` or `GET /carts/me`).

3. **Localized Display Formatting**:
   - Price strings are formatted for display using `Intl.NumberFormat` helpers (e.g., `"150.00"` -> `"150.00 ج.م"` for Arabic or `"EGP 150.00"` for English).

---

## Consequences
* **Positive**:
  - Eliminates floating-point precision bugs completely.
  - Guarantees 100% price consistency between UI displays and backend payment authorization charges.
* **Negative**:
  - Requires developers to parse strings through formatting utility functions rather than using simple string concatenation.

---

## Alternatives Considered
1. **JS Floating Point (`number`)**: *Rejected*: Vulnerable to precision bugs.
2. **Cents Integer Strategy (`15000` cents)**: *Rejected*: Mismatches OpenAPI contract which declares decimal string format (`"150.00"`).
