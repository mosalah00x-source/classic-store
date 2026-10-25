# Backend Implementation Status & Migrations Inventory

## 1. Domain Implementation Matrix

Every domain is classified into one of four states based on backend codebase inspection:
- **IMPLEMENTED**: Fully built with Java Controllers, Services, Repositories, Entities, DTOs, and automated tests.
- **PARTIAL**: Has some working Java classes and endpoints implemented, but key endpoints or logic are missing compared to specification.
- **STUB**: Package or placeholder classes exist, but contain no real business logic.
- **SPEC-ONLY**: Defined in `docs/api/openapi.yaml` and Flyway database schema, but no corresponding Java Controller/Service/Repository classes exist in `backend/src/main/java/com/classicstore/`.

| Domain | Status | Evidence (File Paths & Classes) | Notes / Details |
| :--- | :--- | :--- | :--- |
| **Auth** (register, login, refresh, logout) | **IMPLEMENTED** | `AuthController.java`, `AuthService.java`, `UserRepository.java`, `UserEntity.java`, `JwtService.java`, `JwtAuthenticationFilter.java`, DTOs (`RegisterRequest`, `LoginRequest`, `RefreshTokenRequest`, `AuthResponse`, `RefreshResponse`, `UserResponse`), `IdentityIntegrationTests.java` | Complete JWT HS256 auth implementation with stateless refresh, user registration (CUSTOMER role forced), login with timing-attack prevention, and 204 logout. Tested in `IdentityIntegrationTests` (R01-R13). |
| **Addresses** (`/me/addresses`) | **PARTIAL** | `AddressController.java`, `AddressService.java`, `AddressRepository.java`, `AddressEntity.java`, DTOs (`AddressInput`, `AddressResponse`), `IdentityIntegrationTests.java` | Endpoints `GET /me/addresses`, `POST /me/addresses`, and `GET /me/addresses/{id}` are implemented. **Missing endpoints**: `PUT /me/addresses/{id}`, `DELETE /me/addresses/{id}`, and `PATCH /me/addresses/{id}/default`. |
| **Catalog** (products, categories, brands) | **SPEC-ONLY** | `com/classicstore/catalog/package-info.java`, Flyway `V2__catalog.sql`, `openapi.yaml` | Only `package-info.java` exists in Java code. No controllers, services, repositories, or entities implemented. Specified in `V2` DB schema and `openapi.yaml` (`GET /products`, `GET /products/{slug}`). |
| **Cart** | **SPEC-ONLY** | `com/classicstore/cart/package-info.java`, Flyway `V4__cart_orders_reservations.sql`, `openapi.yaml` | Only `package-info.java` exists. Specified in `V4` DB schema (`store.carts`, `store.cart_items`) and `openapi.yaml` (`POST /carts`, `PUT /carts/{cartId}/items/{sku}`, `DELETE /carts/{cartId}/items/{sku}`). |
| **Checkout** (quote, orders) | **SPEC-ONLY** | `com/classicstore/checkout/package-info.java`, Flyway `V4__cart_orders_reservations.sql`, `openapi.yaml` | Only `package-info.java` exists. Specified in `V4` DB schema and `openapi.yaml` (`POST /checkout/quote`, `POST /checkout/orders`). |
| **Orders** | **SPEC-ONLY** | `com/classicstore/order/package-info.java`, Flyway `V4__cart_orders_reservations.sql`, `openapi.yaml` | Only `package-info.java` exists. Specified in `V4` DB schema (`store.orders`, `store.order_items`, `store.order_status_history`) and `openapi.yaml` (`GET /orders/{id}`, `POST /orders/{id}/cancel`, `GET /guest/orders/{token}`). |
| **Payments and webhooks** | **SPEC-ONLY** | `com/classicstore/payment/package-info.java`, Flyway `V5__payments_shipping_returns_outbox.sql`, `openapi.yaml` | Only `package-info.java` exists. Specified in `V5` DB schema (`store.payments`, `store.payment_events`, `store.refunds`) and `openapi.yaml` (`POST /payments/{id}/webhooks/{provider}`). |
| **Returns and refunds** | **SPEC-ONLY** | `com/classicstore/returns/package-info.java`, Flyway `V5__payments_shipping_returns_outbox.sql`, `openapi.yaml` | Only `package-info.java` exists. Specified in `V5` DB schema (`store.returns`, `store.return_items`) and `openapi.yaml` (`POST /returns`, `POST /admin/refunds`). |
| **Admin** (products, inventory, allocation, fulfillment) | **SPEC-ONLY** | `com/classicstore/admin/package-info.java`, `com/classicstore/fulfillment/package-info.java`, Flyway `V2`, `V3`, `V4`, `V6`, `openapi.yaml` | Only `package-info.java` exists. Specified in `openapi.yaml` (`GET/POST /admin/products`, `GET/POST /admin/inventory`, `POST /admin/orders/{id}/allocate`, `PATCH /admin/fulfillments/{id}/status`). |
| **Inventory and reservations** | **SPEC-ONLY** | `com/classicstore/inventory/package-info.java`, Flyway `V3__inventory.sql`, `V4__cart_orders_reservations.sql`, `V6__functions_allocation.sql`, `V7__integrity_and_reservation_hardening.sql` | Only `package-info.java` exists. DB tables (`store.warehouses`, `store.inventory`, `store.inventory_movements`, `store.inventory_reservations`) and V6/V7 SQL functions exist. |
| **Error contract & Correlation ID** | **IMPLEMENTED** | `GlobalExceptionHandler.java`, `CorrelationIdFilter.java`, `ApiErrorResponse.java`, `BusinessException.java`, `SecurityConfig.java`, `ErrorContractAndCorrelationIdTest.java` | Complete global exception handling returning standardized error schema `{ code, messageAr, messageEn, traceId, details }`. MDC trace ID propagation via `X-Correlation-Id` header. Handled 403 access denied errors. |

---

## 2. Flyway Migrations Inventory

Location: `backend/src/main/resources/db/migration/`

| Migration Version | File Name | One-Line Summary / Tables & Objects Created |
| :--- | :--- | :--- |
| **V1** | `V1__baseline_types_identity.sql` | Extensions (`pgcrypto`, `citext`, `btree_gist`), `store` schema, ENUM types (`user_role`, `product_status`, `order_status`, `payment_status`, `payment_method`, `fulfillment_status`, `reservation_status`, `stock_movement_type`), `store.countries` (seeds EG), `store.governorates`, `store.cities`, `store.users`, `store.addresses` (with partial unique index `ux_addresses_default`), `store.settings`. |
| **V2** | `V2__catalog.sql` | `store.brands`, `store.categories` (hierarchical parent-child), `store.products`, `store.product_categories`, `store.product_variants`, `store.product_images`, `store.coupons` (percentage or fixed discount with rules). |
| **V3** | `V3__inventory.sql` | `store.warehouses` (with geo lat/long), `store.inventory` (warehouse + variant stock with `reserved <= on_hand` check), `store.inventory_movements` (stock audit log). |
| **V4** | `V4__cart_orders_reservations.sql` | `store.carts` (supports `user_id` or `guest_token_hash`), `store.cart_items`, `store.orders` (with generated order number and grand total), `store.order_items`, `store.order_status_history`, `store.fulfillments`, `store.fulfillment_items`, `store.inventory_reservations`. |
| **V5** | `V5__payments_shipping_returns_outbox.sql` | `store.payments` (idempotency key constraint), `store.payment_events`, `store.refunds`, `store.shipping_zones`, `store.shipping_rates`, `store.returns`, `store.return_items`, `store.outbox_events` (transactional outbox pattern). |
| **V6** | `V6__functions_allocation.sql` | PL/pgSQL functions: `store.distance_km` (Haversine formula), `store.rank_warehouses` (rank warehouses by stock availability and proximity), and reservation allocation functions. |
| **V7** | `V7__integrity_and_reservation_hardening.sql` | Hardens inventory reservation constraints (`ux_reservations_active_order_item_warehouse` partial unique index allowing re-reservations after expiry/release while ensuring at most 1 active reservation per item-warehouse), adds cross-entity FKs between `fulfillment_items` and `order_items`. |

### Detailed Notes on V7 Migration
- **Purpose**: Fixes reservation uniqueness to allow re-creating reservations for an `order_item` once previous ones are `RELEASED` or `EXPIRED`, while enforcing max 1 `ACTIVE` reservation per `(order_item_id, warehouse_id)`.
- **Architectural Directive in V7 comments**:
  - `V6` PL/pgSQL allocation functions remain in the database schema.
  - However, the application Java service is designated to own allocation and reservation execution and **MUST NOT** call the `V6` allocation functions.
