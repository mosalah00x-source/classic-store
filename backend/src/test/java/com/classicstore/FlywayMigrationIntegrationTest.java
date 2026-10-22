package com.classicstore;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import org.junit.jupiter.api.Disabled;

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
    void test1_cleanDatabaseFlywayMigrateAppliesV1ToV6Successfully() {
        MigrateResult firstRun = flyway.migrate();
        assertThat(firstRun.success).isTrue();
        assertThat(firstRun.migrationsExecuted).isEqualTo(6);
    }

    @Test
    @Order(2)
    void test2_secondMigrateRunAppliesZeroMigrations() {
        MigrateResult secondRun = flyway.migrate();
        assertThat(secondRun.success).isTrue();
        assertThat(secondRun.migrationsExecuted).isEqualTo(0);
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
    void reserveAfterRelease_sameWarehouse_KnownIssueV7() throws SQLException {
        try (Connection conn = getConnection()) {
            // Seed warehouse
            String whId = "11111111-1111-1111-1111-111111111111";
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.warehouses(id, code, name, address_line, latitude, longitude) " +
                            "VALUES ('" + whId + "', 'WH1', 'Warehouse 1', 'Street 1', 30.0, 31.0)")) {
                ps.executeUpdate();
            }

            // Seed brand, product, variant
            String brandId = "22222222-2222-2222-2222-222222222222";
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.brands(id, name, slug) VALUES ('" + brandId + "', 'Brand1', 'brand1')")) {
                ps.executeUpdate();
            }

            String productId = "33333333-3333-3333-3333-333333333333";
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.products(id, brand_id, name_ar, slug, status) VALUES ('" + productId + "', '" + brandId + "', 'منتج', 'product1', 'ACTIVE')")) {
                ps.executeUpdate();
            }

            String variantId = "44444444-4444-4444-4444-444444444444";
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.product_variants(id, product_id, sku, price) VALUES ('" + variantId + "', '" + productId + "', 'SKU1', 100.00)")) {
                ps.executeUpdate();
            }

            // Seed inventory
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.inventory(warehouse_id, variant_id, on_hand, reserved) VALUES ('" + whId + "', '" + variantId + "', 10, 0)")) {
                ps.executeUpdate();
            }

            // Seed order
            String orderId = "55555555-5555-5555-5555-555555555555";
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.orders(id, guest_phone, customer_name, customer_phone, subtotal, shipping_address, shipping_latitude, shipping_longitude) " +
                            "VALUES ('" + orderId + "', '01000000000', 'Test', '01000000000', 100.00, '{\"country_code\":\"EG\"}'::jsonb, 30.0, 31.0)")) {
                ps.executeUpdate();
            }

            // Seed order item
            String orderItemId = "66666666-6666-6666-6666-666666666666";
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO store.order_items(id, order_id, variant_id, sku_snapshot, product_name_snapshot, quantity, unit_price) " +
                            "VALUES ('" + orderItemId + "', '" + orderId + "', '" + variantId + "', 'SKU1', 'منتج', 1, 100.00)")) {
                ps.executeUpdate();
            }

            // Reserve order
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

            // Try reserve order second time -> expect UNIQUE constraint violation on inventory_reservations (order_item_id, warehouse_id) -> SQLState 23505
            assertThatThrownBy(() -> {
                try (PreparedStatement ps = conn.prepareStatement("SELECT store.reserve_order_at_nearest_warehouse('" + orderId + "'::uuid, 15)")) {
                    ps.executeQuery();
                }
            }).isInstanceOf(SQLException.class)
              .extracting(e -> ((SQLException) e).getSQLState())
              .isEqualTo("23505");
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }
}
