package com.classicstore;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FlywayMigrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final List<String> EXPECTED_32_TABLES = List.of(
            "countries", "governorates", "cities", "users", "addresses", "settings",
            "brands", "categories", "products", "product_categories", "product_variants", "product_images", "coupons",
            "warehouses", "inventory", "inventory_movements",
            "carts", "cart_items", "orders", "order_items", "order_status_history", "fulfillments", "fulfillment_items", "inventory_reservations",
            "payments", "payment_events", "refunds", "shipping_zones", "shipping_rates", "returns", "return_items", "outbox_events"
    );

    private static Flyway flyway;

    @BeforeAll
    static void setUp() {
        flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .schemas("store")
                .defaultSchema("store")
                .createSchemas(true)
                .locations("classpath:db/migration")
                .load();
    }

    @Test
    @Order(1)
    void test1_v6Constraint_and_v7Migration() throws SQLException {
        Flyway flywayV6 = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .schemas("store")
                .defaultSchema("store")
                .createSchemas(true)
                .locations("classpath:db/migration")
                .target("6")
                .load();
        MigrateResult v6Run = flywayV6.migrate();
        assertThat(v6Run.success).isTrue();
        assertThat(v6Run.migrationsExecuted).isEqualTo(6);

        // STEP 2: Record exact constraint name on store.inventory_reservations(order_item_id, warehouse_id) at V6
        String constraintNameAtV6 = null;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT c.conname FROM pg_constraint c " +
                     "JOIN pg_class t ON c.conrelid = t.oid " +
                     "JOIN pg_namespace n ON t.relnamespace = n.oid " +
                     "WHERE n.nspname = 'store' AND t.relname = 'inventory_reservations' AND c.contype = 'u'")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    constraintNameAtV6 = rs.getString(1);
                }
            }
        }
        assertThat(constraintNameAtV6).isEqualTo("inventory_reservations_order_item_id_warehouse_id_key");

        // Now migrate V7
        MigrateResult v7Run = flyway.migrate();
        assertThat(v7Run.success).isTrue();
        assertThat(v7Run.migrationsExecuted).isEqualTo(1);

        // After V7: assert no UNIQUE constraint on those two columns exists
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT c.conname FROM pg_constraint c " +
                     "JOIN pg_class t ON c.conrelid = t.oid " +
                     "JOIN pg_namespace n ON t.relnamespace = n.oid " +
                     "WHERE n.nspname = 'store' AND t.relname = 'inventory_reservations' AND c.contype = 'u'")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertThat(rs.next()).isFalse();
            }
        }

        // Assert partial UNIQUE index ux_inventory_reservations_active_item_warehouse exists with WHERE status = 'ACTIVE'
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT indexdef FROM pg_indexes " +
                     "WHERE schemaname = 'store' AND tablename = 'inventory_reservations' " +
                     "AND indexname = 'ux_inventory_reservations_active_item_warehouse'")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertThat(rs.next()).isTrue();
                String indexdef = rs.getString("indexdef");
                assertThat(indexdef).contains("WHERE");
                assertThat(indexdef).contains("status = 'ACTIVE'");
            }
        }
    }

    @Test
    @Order(2)
    void test2_secondMigrateRunAppliesZeroMigrations() throws SQLException {
        MigrateResult secondRun = flyway.migrate();
        assertThat(secondRun.success).isTrue();
        assertThat(secondRun.migrationsExecuted).isEqualTo(0);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT count(*) FROM store.flyway_schema_history WHERE type = 'SQL' AND success = true");
             ResultSet rs = ps.executeQuery()) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isEqualTo(7);
        }
    }

    @Test
    @Order(3)
    void test3_schemaStoreHasExactly32BaseTables() throws SQLException {
        Set<String> actualTables = new HashSet<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT table_name FROM information_schema.tables " +
                             "WHERE table_schema = 'store' AND table_type = 'BASE TABLE' AND table_name <> 'flyway_schema_history'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                actualTables.add(rs.getString("table_name"));
            }
        }

        assertThat(actualTables).hasSize(32);
        assertThat(actualTables).containsExactlyInAnyOrderElementsOf(EXPECTED_32_TABLES);
    }

    @Test
    @Order(4)
    void test4_columnCountInInformationSchemaEquals251() throws SQLException {
        int columnCount = 0;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT count(*) FROM information_schema.columns " +
                             "WHERE table_schema = 'store' AND table_name <> 'flyway_schema_history'");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                columnCount = rs.getInt(1);
            }
        }

        assertThat(columnCount).isEqualTo(251);
    }

    @Test
    @Order(5)
    void test5_storedFunctionSignaturesExistExactly() throws SQLException {
        Set<String> functions = new HashSet<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT p.proname || '(' || pg_get_function_identity_arguments(p.oid) || ')' AS sig " +
                             "FROM pg_proc p JOIN pg_namespace n ON p.pronamespace = n.oid " +
                             "WHERE n.nspname = 'store'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                functions.add(rs.getString("sig"));
            }
        }

        assertThat(functions).contains(
                "distance_km(lat1 numeric, lon1 numeric, lat2 numeric, lon2 numeric)",
                "rank_warehouses(p_order_id uuid)",
                "reserve_order_at_nearest_warehouse(p_order_id uuid, p_ttl_minutes integer)",
                "release_order_reservations(p_order_id uuid)",
                "consume_order_reservations(p_order_id uuid)"
        );
    }

    @Test
    @Order(6)
    void reserveAfterRelease_sameWarehouse_allowed() throws SQLException {
        try (Connection conn = getConnection()) {
            String whId = "11111111-1111-1111-1111-111111111111";
            String brandId = "22222222-2222-2222-2222-222222222222";
            String productId = "33333333-3333-3333-3333-333333333333";
            String variantId = "44444444-4444-4444-4444-444444444444";
            String orderId = "55555555-5555-5555-5555-555555555555";
            String orderItemId = "66666666-6666-6666-6666-666666666666";

            seedFullOrderStructure(conn, whId, brandId, productId, variantId, orderId, orderItemId);

            // Reserve order first time
            try (PreparedStatement ps = conn.prepareStatement("SELECT store.reserve_order_at_nearest_warehouse('" + orderId + "'::uuid, 15)")) {
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getString(1)).isEqualTo(whId);
                }
            }

            // Release reservation
            try (PreparedStatement ps = conn.prepareStatement("SELECT store.release_order_reservations('" + orderId + "'::uuid)")) {
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt(1)).isEqualTo(1);
                }
            }

            // Reserve order second time -> succeeds!
            try (PreparedStatement ps = conn.prepareStatement("SELECT store.reserve_order_at_nearest_warehouse('" + orderId + "'::uuid, 15)")) {
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getString(1)).isEqualTo(whId);
                }
            }

            // Assert 2 rows in inventory_reservations: first RELEASED, second ACTIVE
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT status FROM store.inventory_reservations WHERE order_id = '" + orderId + "'::uuid ORDER BY created_at ASC")) {
                try (ResultSet rs = ps.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getString("status")).isEqualTo("RELEASED");
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getString("status")).isEqualTo("ACTIVE");
                }
            }
        }
    }

    @Test
    @Order(7)
    void test4_1_twoActiveReservationsForSameOrderItemAndWarehouse_failWith23505() throws SQLException {
        try (Connection conn = getConnection()) {
            String whId = UUID.randomUUID().toString();
            String brandId = UUID.randomUUID().toString();
            String productId = UUID.randomUUID().toString();
            String variantId = UUID.randomUUID().toString();
            String orderId = UUID.randomUUID().toString();
            String orderItemId = UUID.randomUUID().toString();

            seedFullOrderStructure(conn, whId, brandId, productId, variantId, orderId, orderItemId);

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.inventory_reservations(order_id, order_item_id, warehouse_id, variant_id, quantity, status, expires_at) " +
                    "VALUES ('" + orderId + "', '" + orderItemId + "', '" + whId + "', '" + variantId + "', 1, 'ACTIVE', now() + interval '15 mins')")) {
                ps.executeUpdate();
            }

            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO store.inventory_reservations(order_id, order_item_id, warehouse_id, variant_id, quantity, status, expires_at) " +
                        "VALUES ('" + orderId + "', '" + orderItemId + "', '" + whId + "', '" + variantId + "', 1, 'ACTIVE', now() + interval '15 mins')")) {
                    ps.executeUpdate();
                }
            }).isInstanceOf(SQLException.class)
              .extracting(e -> ((SQLException) e).getSQLState())
              .isEqualTo("23505");
        }
    }

    @Test
    @Order(8)
    void test4_2_activeToExpiredToNewActive_succeeds() throws SQLException {
        try (Connection conn = getConnection()) {
            String whId = UUID.randomUUID().toString();
            String brandId = UUID.randomUUID().toString();
            String productId = UUID.randomUUID().toString();
            String variantId = UUID.randomUUID().toString();
            String orderId = UUID.randomUUID().toString();
            String orderItemId = UUID.randomUUID().toString();

            seedFullOrderStructure(conn, whId, brandId, productId, variantId, orderId, orderItemId);

            String res1Id = UUID.randomUUID().toString();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.inventory_reservations(id, order_id, order_item_id, warehouse_id, variant_id, quantity, status, expires_at) " +
                    "VALUES ('" + res1Id + "', '" + orderId + "', '" + orderItemId + "', '" + whId + "', '" + variantId + "', 1, 'ACTIVE', now() - interval '1 min')")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE store.inventory_reservations SET status = 'EXPIRED' WHERE id = '" + res1Id + "'")) {
                ps.executeUpdate();
            }

            String res2Id = UUID.randomUUID().toString();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.inventory_reservations(id, order_id, order_item_id, warehouse_id, variant_id, quantity, status, expires_at) " +
                    "VALUES ('" + res2Id + "', '" + orderId + "', '" + orderItemId + "', '" + whId + "', '" + variantId + "', 1, 'ACTIVE', now() + interval '15 mins')")) {
                int inserted = ps.executeUpdate();
                assertThat(inserted).isEqualTo(1);
            }
        }
    }

    @Test
    @Order(9)
    void test4_3_reservationVariantMismatch_fails() throws SQLException {
        try (Connection conn = getConnection()) {
            String whId = UUID.randomUUID().toString();
            String brandId = UUID.randomUUID().toString();
            String productId = UUID.randomUUID().toString();
            String variantId1 = UUID.randomUUID().toString();
            String variantId2 = UUID.randomUUID().toString();
            String orderId = UUID.randomUUID().toString();
            String orderItemId = UUID.randomUUID().toString();

            seedFullOrderStructure(conn, whId, brandId, productId, variantId1, orderId, orderItemId);

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.product_variants(id, product_id, sku, price) VALUES ('" + variantId2 + "', '" + productId + "', 'SKU_" + variantId2.substring(0, 8) + "', 120.00)")) {
                ps.executeUpdate();
            }

            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO store.inventory_reservations(order_id, order_item_id, warehouse_id, variant_id, quantity, status, expires_at) " +
                        "VALUES ('" + orderId + "', '" + orderItemId + "', '" + whId + "', '" + variantId2 + "', 1, 'ACTIVE', now() + interval '15 mins')")) {
                    ps.executeUpdate();
                }
            }).isInstanceOf(SQLException.class)
              .hasMessageContaining("Reservation item, variant and order mismatch");
        }
    }

    @Test
    @Order(10)
    void test4_4_reservationOrderMismatch_fails() throws SQLException {
        try (Connection conn = getConnection()) {
            String whId = UUID.randomUUID().toString();
            String brandId = UUID.randomUUID().toString();
            String productId = UUID.randomUUID().toString();
            String variantId = UUID.randomUUID().toString();
            String orderId1 = UUID.randomUUID().toString();
            String orderId2 = UUID.randomUUID().toString();
            String orderItemId1 = UUID.randomUUID().toString();

            seedFullOrderStructure(conn, whId, brandId, productId, variantId, orderId1, orderItemId1);

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.orders(id, guest_phone, customer_name, customer_phone, subtotal, shipping_address, shipping_latitude, shipping_longitude) " +
                    "VALUES ('" + orderId2 + "', '01000000000', 'Test2', '01000000000', 100.00, '{\"country_code\":\"EG\"}'::jsonb, 30.0, 31.0)")) {
                ps.executeUpdate();
            }

            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO store.inventory_reservations(order_id, order_item_id, warehouse_id, variant_id, quantity, status, expires_at) " +
                        "VALUES ('" + orderId2 + "', '" + orderItemId1 + "', '" + whId + "', '" + variantId + "', 1, 'ACTIVE', now() + interval '15 mins')")) {
                    ps.executeUpdate();
                }
            }).isInstanceOf(SQLException.class)
              .hasMessageContaining("Reservation item, variant and order mismatch");
        }
    }

    @Test
    @Order(11)
    void test4_5_fulfillmentItemOtherOrder_fails() throws SQLException {
        try (Connection conn = getConnection()) {
            String whId = UUID.randomUUID().toString();
            String brandId = UUID.randomUUID().toString();
            String productId = UUID.randomUUID().toString();
            String variantId = UUID.randomUUID().toString();
            String orderId1 = UUID.randomUUID().toString();
            String orderItemId1 = UUID.randomUUID().toString();

            seedFullOrderStructure(conn, whId, brandId, productId, variantId, orderId1, orderItemId1);

            String orderId2 = UUID.randomUUID().toString();
            String orderItemId2 = UUID.randomUUID().toString();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.orders(id, guest_phone, customer_name, customer_phone, subtotal, shipping_address, shipping_latitude, shipping_longitude) " +
                    "VALUES ('" + orderId2 + "', '01000000000', 'Test2', '01000000000', 100.00, '{\"country_code\":\"EG\"}'::jsonb, 30.0, 31.0)")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.order_items(id, order_id, variant_id, sku_snapshot, product_name_snapshot, quantity, unit_price) " +
                    "VALUES ('" + orderItemId2 + "', '" + orderId2 + "', '" + variantId + "', 'SKU2', 'منتج', 1, 100.00)")) {
                ps.executeUpdate();
            }

            String fulfillmentId1 = UUID.randomUUID().toString();
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.fulfillments(id, order_id, warehouse_id, status) VALUES ('" + fulfillmentId1 + "', '" + orderId1 + "', '" + whId + "', 'PENDING')")) {
                ps.executeUpdate();
            }

            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO store.fulfillment_items(fulfillment_id, order_item_id, quantity) VALUES ('" + fulfillmentId1 + "', '" + orderItemId2 + "', 1)")) {
                    ps.executeUpdate();
                }
            }).isInstanceOf(SQLException.class)
              .hasMessageContaining("Fulfillment item must belong to fulfillment order");
        }
    }

    @Test
    @Order(12)
    void test4_6_updateOrderItemAfterReservation_fails() throws SQLException {
        try (Connection conn = getConnection()) {
            String whId = UUID.randomUUID().toString();
            String brandId = UUID.randomUUID().toString();
            String productId = UUID.randomUUID().toString();
            String variantId = UUID.randomUUID().toString();
            String orderId = UUID.randomUUID().toString();
            String orderItemId = UUID.randomUUID().toString();

            seedFullOrderStructure(conn, whId, brandId, productId, variantId, orderId, orderItemId);

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.inventory_reservations(order_id, order_item_id, warehouse_id, variant_id, quantity, status, expires_at) " +
                    "VALUES ('" + orderId + "', '" + orderItemId + "', '" + whId + "', '" + variantId + "', 1, 'ACTIVE', now() + interval '15 mins')")) {
                ps.executeUpdate();
            }

            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE store.order_items SET quantity = 2 WHERE id = '" + orderItemId + "'")) {
                    ps.executeUpdate();
                }
            }).isInstanceOf(SQLException.class)
              .hasMessageContaining("Order items are immutable after allocation");
        }
    }

    @Test
    @Order(13)
    void test4_7_updateOrderItemWithoutReservation_succeeds() throws SQLException {
        try (Connection conn = getConnection()) {
            String whId = UUID.randomUUID().toString();
            String brandId = UUID.randomUUID().toString();
            String productId = UUID.randomUUID().toString();
            String variantId = UUID.randomUUID().toString();
            String orderId = UUID.randomUUID().toString();
            String orderItemId = UUID.randomUUID().toString();

            seedFullOrderStructure(conn, whId, brandId, productId, variantId, orderId, orderItemId);

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE store.order_items SET quantity = 5 WHERE id = '" + orderItemId + "'")) {
                int updated = ps.executeUpdate();
                assertThat(updated).isEqualTo(1);
            }
        }
    }

    @Test
    @Order(14)
    void test4_8_movingOrderItemToAnotherOrder_fails() throws SQLException {
        try (Connection conn = getConnection()) {
            String whId = UUID.randomUUID().toString();
            String brandId = UUID.randomUUID().toString();
            String productId = UUID.randomUUID().toString();
            String variantId = UUID.randomUUID().toString();
            String orderId1 = UUID.randomUUID().toString();
            String orderId2 = UUID.randomUUID().toString();
            String orderItemId = UUID.randomUUID().toString();

            seedFullOrderStructure(conn, whId, brandId, productId, variantId, orderId1, orderItemId);

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.orders(id, guest_phone, customer_name, customer_phone, subtotal, shipping_address, shipping_latitude, shipping_longitude) " +
                    "VALUES ('" + orderId2 + "', '01000000000', 'Test2', '01000000000', 100.00, '{\"country_code\":\"EG\"}'::jsonb, 30.0, 31.0)")) {
                ps.executeUpdate();
            }

            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE store.order_items SET order_id = '" + orderId2 + "' WHERE id = '" + orderItemId + "'")) {
                    ps.executeUpdate();
                }
            }).isInstanceOf(SQLException.class)
              .hasMessageContaining("Moving an order item between orders is prohibited");
        }
    }

    @Test
    @Order(15)
    void test5_concurrency_twoThreadsReservingSameOrder() throws Exception {
        String whId = UUID.randomUUID().toString();
        String brandId = UUID.randomUUID().toString();
        String productId = UUID.randomUUID().toString();
        String variantId = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();
        String orderItemId = UUID.randomUUID().toString();

        try (Connection conn = getConnection()) {
            seedFullOrderStructure(conn, whId, brandId, productId, variantId, orderId, orderItemId);
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicReference<String> result1 = new AtomicReference<>();
        AtomicReference<Throwable> exception1 = new AtomicReference<>();

        AtomicReference<String> result2 = new AtomicReference<>();
        AtomicReference<Throwable> exception2 = new AtomicReference<>();

        Runnable reserveTask1 = () -> {
            try {
                startLatch.await();
                try (Connection conn = getConnection()) {
                    try (PreparedStatement ps = conn.prepareStatement("SELECT store.reserve_order_at_nearest_warehouse('" + orderId + "'::uuid, 15)")) {
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                result1.set(rs.getString(1));
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                exception1.set(t);
            } finally {
                doneLatch.countDown();
            }
        };

        Runnable reserveTask2 = () -> {
            try {
                startLatch.await();
                try (Connection conn = getConnection()) {
                    try (PreparedStatement ps = conn.prepareStatement("SELECT store.reserve_order_at_nearest_warehouse('" + orderId + "'::uuid, 15)")) {
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                result2.set(rs.getString(1));
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                exception2.set(t);
            } finally {
                doneLatch.countDown();
            }
        };

        executor.submit(reserveTask1);
        executor.submit(reserveTask2);

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        boolean thread1Succeeded = result1.get() != null && result1.get().equals(whId) && exception1.get() == null;
        boolean thread2Succeeded = result2.get() != null && result2.get().equals(whId) && exception2.get() == null;

        assertThat(thread1Succeeded ^ thread2Succeeded).isTrue();

        if (thread1Succeeded) {
            boolean thread2FailedAsExpected = (result2.get() == null) ||
                    (exception2.get() != null && exception2.get().getMessage().contains("Order already reserved"));
            assertThat(thread2FailedAsExpected).isTrue();
        } else {
            boolean thread1FailedAsExpected = (result1.get() == null) ||
                    (exception1.get() != null && exception1.get().getMessage().contains("Order already reserved"));
            assertThat(thread1FailedAsExpected).isTrue();
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT on_hand, reserved FROM store.inventory WHERE warehouse_id = '" + whId + "'::uuid AND variant_id = '" + variantId + "'::uuid");
             ResultSet rs = ps.executeQuery()) {
            assertThat(rs.next()).isTrue();
            int onHand = rs.getInt("on_hand");
            int reserved = rs.getInt("reserved");
            assertThat(reserved).isLessThanOrEqualTo(onHand);
        }
    }

    private void seedFullOrderStructure(Connection conn, String whId, String brandId, String productId, String variantId, String orderId, String orderItemId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO store.warehouses(id, code, name, address_line, latitude, longitude) " +
                "VALUES ('" + whId + "', 'WH_" + whId.substring(0, 8) + "', 'Warehouse', 'Street 1', 30.0, 31.0)")) {
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO store.brands(id, name, slug) VALUES ('" + brandId + "', 'Brand_" + brandId.substring(0, 8) + "', 'brand_" + brandId.substring(0, 8) + "')")) {
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO store.products(id, brand_id, name_ar, slug, status) VALUES ('" + productId + "', '" + brandId + "', 'منتج', 'product_" + productId.substring(0, 8) + "', 'ACTIVE')")) {
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO store.product_variants(id, product_id, sku, price) VALUES ('" + variantId + "', '" + productId + "', 'SKU_" + variantId.substring(0, 8) + "', 100.00)")) {
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO store.inventory(warehouse_id, variant_id, on_hand, reserved) VALUES ('" + whId + "', '" + variantId + "', 10, 0)")) {
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO store.orders(id, guest_phone, customer_name, customer_phone, subtotal, shipping_address, shipping_latitude, shipping_longitude) " +
                "VALUES ('" + orderId + "', '01000000000', 'Test', '01000000000', 100.00, '{\"country_code\":\"EG\"}'::jsonb, 30.0, 31.0)")) {
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO store.order_items(id, order_id, variant_id, sku_snapshot, product_name_snapshot, quantity, unit_price) " +
                "VALUES ('" + orderItemId + "', '" + orderId + "', '" + variantId + "', 'SKU_" + variantId.substring(0, 8) + "', 'منتج', 1, 100.00)")) {
            ps.executeUpdate();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }
}
