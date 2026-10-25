# OpenAPI Specification Audit & Operations Inventory

## 1. Specification Overview
- **File Path**: `docs/api/openapi.yaml`
- **OpenAPI Version**: `3.1.0`
- **Title**: Classic Store API
- **Version**: `1.0.0`
- **Server Base URL**: `https://api.example.com/api/v1` (Base path `/api/v1`)
- **Total Paths**: `23`
- **Total Operations**: `26`

---

## 2. Redocly Linter Output
Ran linter using `npx @redocly/cli lint docs/api/openapi.yaml --config docs/api/.redocly.yaml`:
- **Errors**: `0`
- **Warnings**: `2`
  1. `docs/api/openapi.yaml:13:3` at `#/info/license`: License object should contain `url` or `identifier` (`info-license-strict`).
  2. `docs/api/openapi.yaml:16:10` at `#/servers/0/url`: Server URL points to `example.com` (`no-server-example-com`).

---

## 3. Full Operations Inventory Table (26 Operations)

| Method | Path | operationId | Tags | Auth Requirement | Request Body Schema | Success Response | Error Responses |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **GET** | `/products` | `listProducts` | Catalog | None | None | 200: `PagedResponse<ProductResponse>` | 400, 429, 500 |
| **GET** | `/products/{slug}` | `getProductBySlug` | Catalog | None | None | 200: `ProductResponse` | 404, 500 |
| **POST** | `/auth/register` | `registerCustomer` | Auth | None | `RegisterRequest` | 201: `UserResponse` | 400, 409, 422, 500 |
| **POST** | `/auth/login` | `login` | Auth | None | `LoginRequest` | 200: `AuthResponse` | 400, 401, 429, 500 |
| **POST** | `/auth/refresh` | `refreshToken` | Auth | None | `RefreshTokenRequest` | 200: `RefreshResponse` | 400, 401, 500 |
| **POST** | `/auth/logout` | `logout` | Auth | `bearerAuth` | None | 204: No Content | 401, 500 |
| **GET** | `/me/addresses` | `getCustomerAddresses` | Shipping | `bearerAuth` | None | 200: `AddressResponse[]` | 401, 500 |
| **POST** | `/me/addresses` | `addCustomerAddress` | Shipping | `bearerAuth` | `AddressInput` | 201: `AddressResponse` | 400, 401, 422, 500 |
| **GET** | `/me/addresses/{addressId}` | `getCustomerAddressById` | Shipping | `bearerAuth` | None | 200: `AddressResponse` | 401, 404, 500 |
| **POST** | `/carts` | `createCart` | Cart | `bearerAuth` | None | 201: `CartResponse` | 400, 500 |
| **PUT** | `/carts/{cartId}/items/{sku}` | `updateCartItem` | Cart | `bearerAuth`, `guestCartToken` | `{ quantity }` | 200: `CartResponse` | 400, 401, 404, 422, 500 |
| **DELETE** | `/carts/{cartId}/items/{sku}` | `removeCartItem` | Cart | `bearerAuth`, `guestCartToken` | None | 200: `CartResponse` | 401, 404, 500 |
| **POST** | `/checkout/quote` | `calculateCheckoutQuote` | Checkout | `bearerAuth`, `guestCartToken` | `CheckoutQuoteRequest` | 200: `CheckoutQuoteResponse` | 400, 404, 422, 500 |
| **POST** | `/checkout/orders` | `createCheckoutOrder` | Checkout | `bearerAuth`, `guestCartToken` | `CreateOrderRequest` | 201: `OrderResponse` | 400, 409, 422, 429, 500 |
| **GET** | `/orders/{orderId}` | `getOrderById` | Orders | `bearerAuth` | None | 200: `OrderResponse` | 401, 403, 404, 500 |
| **POST** | `/orders/{orderId}/cancel` | `cancelOrder` | Orders | `bearerAuth` | `{ reason }` | 200: `OrderResponse` | 400, 401, 403, 404, 409, 500 |
| **GET** | `/guest/orders/{token}` | `getGuestOrder` | Orders | None | None | 200: `OrderResponse` | 401, 404, 500 |
| **POST** | `/payments/{paymentId}/webhooks/{provider}` | `handlePaymentWebhook` | Payments | None | `{ payload }` | 200: `{ received: true }` | 400, 401, 500 |
| **GET** | `/admin/products` | `adminListProducts` | Admin | `bearerAuth` | None | 200: `PagedResponse<ProductResponse>` | 401, 403, 500 |
| **POST** | `/admin/products` | `adminCreateProduct` | Admin | `bearerAuth` | `ProductInput` | 201: `ProductResponse` | 400, 401, 403, 422, 500 |
| **GET** | `/admin/inventory` | `adminGetInventory` | Inventory | `bearerAuth` | None | 200: `PagedResponse<InventoryResponse>` | 401, 403, 500 |
| **POST** | `/admin/inventory/adjustments` | `adminAdjustInventory` | Inventory | `bearerAuth` | `InventoryAdjustmentRequest` | 200: `InventoryResponse` | 400, 401, 403, 422, 500 |
| **POST** | `/admin/orders/{orderId}/allocate` | `adminAllocateOrder` | Admin | `bearerAuth` | `{ warehouseId }` | 200: `AllocationResponse` | 400, 401, 403, 404, 409, 500 |
| **PATCH** | `/admin/fulfillments/{fulfillmentId}/status` | `adminUpdateFulfillmentStatus` | Shipping | `bearerAuth` | `{ status, trackingNumber }` | 200: `FulfillmentResponse` | 400, 401, 403, 404, 500 |
| **POST** | `/returns` | `createReturn` | Returns | `bearerAuth` | `ReturnRequest` | 201: `ReturnResponse` | 400, 401, 404, 422, 500 |
| **POST** | `/admin/refunds` | `adminProcessRefund` | Admin | `bearerAuth` | `RefundRequest` | 201: `RefundResponse` | 400, 401, 403, 404, 422, 500 |

---

## 4. Key Schemas & Data Conventions

### Security Schemes
1. **`bearerAuth`**:
   - Type: `http` (`scheme: bearer`, `bearerFormat: JWT`).
   - Used for authenticated customer (`CUSTOMER`) and admin (`ADMIN`) routes.
2. **`guestCartToken`**:
   - Type: `apiKey` (`in: header`, `name: X-Guest-Token`).
   - Used for unauthenticated cart and checkout operations.

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
- **`Idempotency-Key`**: Required header on `POST /checkout/orders` to ensure safe order placement retries.
- **`X-Guest-Token`**: Header for guest cart and checkout session tracking.
- **`X-Signature`**: Webhook HMAC signature header on `POST /payments/{paymentId}/webhooks/{provider}`.

---

## 5. Specification vs Implementation Comparison

### Operations in Spec with NO Controller Implementation (23 Operations)
All operations EXCEPT `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`, `GET /me/addresses`, `POST /me/addresses`, and `GET /me/addresses/{addressId}` are currently **SPEC-ONLY**.

### Controller Endpoints NOT in OpenAPI Spec
None. Every implemented controller endpoint exists in the OpenAPI spec.

### DTO Field & Endpoint Mismatches
1. **Address Endpoints**:
   - Spec defines 6 address endpoints (`GET`, `POST`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`, `PATCH /{id}/default`).
   - Implementation only provides 3 (`GET`, `POST`, `GET /{id}`). Missing: `PUT`, `DELETE`, `PATCH default`.
2. **Address DTO Fields**:
   - `AddressInput` / `AddressResponse` in Java code include `line1` as an alias for `street`, whereas OpenAPI spec defines `street`.
3. **Category Reference API**:
   - Static prototype relies heavily on category tree navigation (mega menu).
   - OpenAPI spec has NO `GET /categories` or `GET /categories/tree` endpoint.
4. **Governorates & Cities Reference API**:
   - Address forms require governorate and city selection (`governorateId`, `cityId`).
   - OpenAPI spec has NO reference endpoints to list governorates or cities.
