# OpenAPI Specification Audit & Operations Inventory

## 1. Specification Overview
- **File Path**: `docs/api/openapi.yaml`
- **OpenAPI Version**: `3.1.0`
- **Title**: Classic Store API
- **Version**: `1.0.0`
- **Server Base URL**: `https://api.example.com/api/v1` (Base path `/api/v1`)
- **Total Paths**: `23`
- **Total Operations**: `26` (7 Implemented, 19 Spec-Only)

---

## 2. Redocly Linter Output
Ran linter using `npx @redocly/cli lint docs/api/openapi.yaml --config docs/api/.redocly.yaml`:
- **Errors**: `0`
- **Warnings**: `2`
  1. `docs/api/openapi.yaml:13:3` at `#/info/license`: License object should contain `url` or `identifier` (`info-license-strict`).
  2. `docs/api/openapi.yaml:16:10` at `#/servers/0/url`: Server URL points to `example.com` (`no-server-example-com`).

---

## 3. Full Operations Inventory Table (26 Operations)

| Method | Path | operationId | Tags | Implementation Status | Auth Requirement | Request Body Schema | Success Response | Error Responses | Evidence / Controller Class |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **GET** | `/products` | `listProducts` | Catalog | **SPEC-ONLY** | None | None | 200: `PagedResponse<ProductResponse>` | 400, 429, 500 | `docs/api/openapi.yaml:46` |
| **GET** | `/products/{slug}` | `getProductBySlug` | Catalog | **SPEC-ONLY** | None | None | 200: `ProductResponse` | 404, 500 | `docs/api/openapi.yaml:127` |
| **POST** | `/auth/register` | `registerCustomer` | Auth | **IMPLEMENTED** | None | `RegisterRequest` | 201: `UserResponse` | 400, 409, 422, 500 | `AuthController.java:23` |
| **POST** | `/auth/login` | `login` | Auth | **IMPLEMENTED** | None | `LoginRequest` | 200: `AuthResponse` | 400, 401, 429, 500 | `AuthController.java:29` |
| **POST** | `/auth/refresh` | `refreshToken` | Auth | **IMPLEMENTED** | None | `RefreshTokenRequest` | 200: `RefreshResponse` | 400, 401, 500 | `AuthController.java:35` |
| **POST** | `/auth/logout` | `logout` | Auth | **IMPLEMENTED** | `bearerAuth` | None | 204: No Content | 401, 500 | `AuthController.java:41` |
| **GET** | `/me/addresses` | `getCustomerAddresses` | Shipping | **IMPLEMENTED** | `bearerAuth` | None | 200: `AddressResponse[]` | 401, 500 | `AddressController.java:29` |
| **POST** | `/me/addresses` | `addCustomerAddress` | Shipping | **IMPLEMENTED** | `bearerAuth` | `AddressInput` | 201: `AddressResponse` | 400, 401, 422, 500 | `AddressController.java:36` |
| **GET** | `/me/addresses/{addressId}` | `getCustomerAddressById` | Shipping | **IMPLEMENTED** | `bearerAuth` | None | 200: `AddressResponse` | 401, 404, 500 | `AddressController.java:43` |
| **POST** | `/carts` | `createCart` | Cart | **SPEC-ONLY** | None / `bearerAuth` | None | 201: `CartResponse` | 400, 500 | `docs/api/openapi.yaml:373` |
| **PUT** | `/carts/{cartId}/items/{sku}` | `updateCartItem` | Cart | **SPEC-ONLY** | `bearerAuth`, `guestCartToken` | `{ quantity }` | 200: `CartResponse` | 400, 401, 404, 422, 500 | `docs/api/openapi.yaml:394` |
| **DELETE** | `/carts/{cartId}/items/{sku}` | `removeCartItem` | Cart | **SPEC-ONLY** | `bearerAuth`, `guestCartToken` | None | 200: `CartResponse` | 401, 404, 500 | `docs/api/openapi.yaml:452` |
| **POST** | `/checkout/quote` | `calculateCheckoutQuote` | Checkout | **SPEC-ONLY** | `bearerAuth`, `guestCartToken` | `CheckoutQuoteRequest` | 200: `CheckoutQuoteResponse` | 400, 404, 422, 500 | `docs/api/openapi.yaml:483` |
| **POST** | `/checkout/orders` | `createCheckoutOrder` | Checkout | **SPEC-ONLY** | `bearerAuth`, `guestCartToken` | `CreateOrderRequest` | 201: `OrderResponse` | 400, 409, 422, 429, 500 | `docs/api/openapi.yaml:528` |
| **GET** | `/orders/{orderId}` | `getOrderById` | Orders | **SPEC-ONLY** | `bearerAuth` | None | 200: `OrderResponse` | 401, 403, 404, 500 | `docs/api/openapi.yaml:630` |
| **POST** | `/orders/{orderId}/cancel` | `cancelOrder` | Orders | **SPEC-ONLY** | `bearerAuth` | `{ reason }` | 200: `OrderResponse` | 400, 401, 403, 404, 409, 500 | `docs/api/openapi.yaml:666` |
| **GET** | `/guest/orders/{token}` | `getGuestOrder` | Orders | **SPEC-ONLY** | None | None | 200: `OrderResponse` | 401, 404, 500 | `docs/api/openapi.yaml:598` |
| **POST** | `/payments/{paymentId}/webhooks/{provider}` | `handlePaymentWebhook` | Payments | **SPEC-ONLY** | None | `{ payload }` | 200: `{ received: true }` | 400, 401, 500 | `docs/api/openapi.yaml:810` |
| **GET** | `/admin/products` | `adminListProducts` | Admin | **SPEC-ONLY** | `bearerAuth` | None | 200: `PagedResponse<ProductResponse>` | 401, 403, 500 | `docs/api/openapi.yaml:851` |
| **POST** | `/admin/products` | `adminCreateProduct` | Admin | **SPEC-ONLY** | `bearerAuth` | `ProductInput` | 201: `ProductResponse` | 400, 401, 403, 422, 500 | `docs/api/openapi.yaml:892` |
| **GET** | `/admin/inventory` | `adminGetInventory` | Inventory | **SPEC-ONLY** | `bearerAuth` | None | 200: `PagedResponse<InventoryResponse>` | 401, 403, 500 | `docs/api/openapi.yaml:935` |
| **POST** | `/admin/inventory/adjustments` | `adminAdjustInventory` | Inventory | **SPEC-ONLY** | `bearerAuth` | `InventoryAdjustmentRequest` | 200: `InventoryResponse` | 400, 401, 403, 422, 500 | `docs/api/openapi.yaml:977` |
| **POST** | `/admin/orders/{orderId}/allocate` | `adminAllocateOrder` | Admin | **SPEC-ONLY** | `bearerAuth` | `{ warehouseId }` | 200: `AllocationResponse` | 400, 401, 403, 404, 409, 500 | `docs/api/openapi.yaml:708` |
| **PATCH** | `/admin/fulfillments/{fulfillmentId}/status` | `adminUpdateFulfillmentStatus` | Shipping | **SPEC-ONLY** | `bearerAuth` | `{ status, trackingNumber }` | 200: `FulfillmentResponse` | 400, 401, 403, 404, 500 | `docs/api/openapi.yaml:754` |
| **POST** | `/returns` | `createReturn` | Returns | **SPEC-ONLY** | `bearerAuth` | `ReturnRequest` | 201: `ReturnResponse` | 400, 401, 404, 422, 500 | `docs/api/openapi.yaml:1022` |
| **POST** | `/admin/refunds` | `adminProcessRefund` | Admin | **SPEC-ONLY** | `bearerAuth` | `RefundRequest` | 201: `RefundResponse` | 400, 401, 403, 404, 422, 500 | `docs/api/openapi.yaml:1062` |

---

## 4. Key Schemas & Data Conventions

### Security Schemes
1. **`bearerAuth`**:
   - Type: `http` (`scheme: bearer`, `bearerFormat: JWT`).
   - Used for authenticated customer (`CUSTOMER`) and admin (`ADMIN`) routes (`docs/api/openapi.yaml:1249`).
2. **`guestCartToken`**:
   - Type: `apiKey` (`in: header`, `name: X-Guest-Cart-Token`).
   - Used for unauthenticated cart and checkout operations (`docs/api/openapi.yaml:1255`).

### Money Format & Currency
- Money values are represented as Strings with exactly 2 decimal places matching pattern `^\d+\.\d{2}$` (e.g., `"150.00"`).
- Currency default is `"EGP"` across all financial fields.

### Pagination Schema (`PagedResponse`)
```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

### Standard Error Schema (`ApiErrorResponse`)
```json
{
  "code": "VALIDATION_ERROR",
  "messageAr": "خطأ في البيانات المدخلة",
  "messageEn": "Validation error",
  "traceId": "c1a2b3c4-5678-90ab-cdef-1234567890ab",
  "details": {}
}
```

### Headers Declared
- **`X-Correlation-Id`**: Optional request header, echoed back in all response headers and embedded in `ApiErrorResponse.traceId`.
- **`Idempotency-Key`**: Required header on `POST /checkout/orders` (`docs/api/openapi.yaml:545`) to ensure safe order placement retries.
- **`X-Guest-Cart-Token`**: Header for guest cart and checkout session tracking (`docs/api/openapi.yaml:1255`).
- **`X-Signature`**: Webhook HMAC signature header on `POST /payments/{paymentId}/webhooks/{provider}` (`docs/api/openapi.yaml:823`).

---

## 5. Specification vs Implementation Comparison

### Implemented Operations (7 Operations)
- `POST /auth/register` (`AuthController.java`)
- `POST /auth/login` (`AuthController.java`)
- `POST /auth/refresh` (`AuthController.java`)
- `POST /auth/logout` (`AuthController.java`)
- `GET /me/addresses` (`AddressController.java`)
- `POST /me/addresses` (`AddressController.java`)
- `GET /me/addresses/{addressId}` (`AddressController.java`)

### Operations in Spec with NO Controller Implementation (19 Operations)
The remaining 19 operations declared in `docs/api/openapi.yaml` are currently **SPEC-ONLY** (no Java controllers or services exist in `backend/src/main/java/com/classicstore/`).

### Controller Endpoints NOT in OpenAPI Spec
None. Every implemented controller endpoint exists in the OpenAPI spec.

### DTO Field & Endpoint Mismatches
1. **Address Endpoints**:
   - `openapi.yaml` defines only 3 address endpoints (`GET /me/addresses`, `POST /me/addresses`, `GET /me/addresses/{addressId}`).
   - `PUT /me/addresses/{id}`, `DELETE /me/addresses/{id}`, and `PATCH /me/addresses/{id}/default` are **missing in BOTH the OpenAPI specification and the backend implementation**.
2. **Address DTO Fields**:
   - `AddressInput.java` / `AddressResponse.java` in Java code include `line1` as an alias for `street`, whereas OpenAPI spec defines `street`.
3. **Category Reference API**:
   - Static prototype relies heavily on category tree navigation (mega menu).
   - OpenAPI spec has NO `GET /categories` or `GET /categories/tree` endpoint.
4. **Governorates & Cities Reference API**:
   - Address forms require governorate and city selection (`governorateId`, `cityId`).
   - OpenAPI spec has NO reference endpoints to list governorates or cities.
