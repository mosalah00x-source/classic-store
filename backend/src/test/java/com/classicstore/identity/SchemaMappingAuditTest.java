package com.classicstore.identity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;
import org.hibernate.annotations.JdbcTypeCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class SchemaMappingAuditTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.schemas", () -> "store");
        registry.add("spring.flyway.default-schema", () -> "store");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("jwt.secret", () -> "super-secret-jwt-key-for-testing-must-be-at-least-32-bytes-long");
    }

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void auditSchemaMappings() {
        System.out.println("=== SCHEMA MAPPING AUDIT TABLE START ===");

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT table_name, column_name, data_type, udt_name " +
                "FROM information_schema.columns " +
                "WHERE table_schema = 'store'"
        );

        Map<String, Map<String, String>> dbColumnTypes = new HashMap<>();
        for (Map<String, Object> col : columns) {
            String tableName = (String) col.get("table_name");
            String columnName = (String) col.get("column_name");
            String dataType = (String) col.get("data_type");
            String udtName = (String) col.get("udt_name");
            dbColumnTypes.computeIfAbsent(tableName, k -> new HashMap<>()).put(columnName, dataType + " (" + udtName + ")");
        }

        List<String> mismatchLog = new ArrayList<>();

        for (EntityType<?> entity : entityManagerFactory.getMetamodel().getEntities()) {
            Class<?> javaType = entity.getJavaType();
            if (!javaType.getPackageName().startsWith("com.classicstore.identity")) {
                continue;
            }

            jakarta.persistence.Table tableAnn = javaType.getAnnotation(jakarta.persistence.Table.class);
            String tableName = tableAnn != null ? tableAnn.name() : javaType.getSimpleName().toLowerCase();

            for (Field field : javaType.getDeclaredFields()) {
                Column colAnn = field.getAnnotation(Column.class);
                if (colAnn != null) {
                    String columnName = colAnn.name();
                    String dbType = dbColumnTypes.getOrDefault(tableName, Map.of()).get(columnName);

                    JdbcTypeCode jdbcTypeCode = field.getAnnotation(JdbcTypeCode.class);
                    String mappedType = jdbcTypeCode != null ? "SqlTypes." + jdbcTypeCode.value() : field.getType().getSimpleName();

                    System.out.printf("ENTITY: %-15s | ATTR: %-15s | COL: %-15s | DB_TYPE: %-25s | MAPPED_TYPE: %s%n",
                            entity.getName(), field.getName(), columnName, dbType, mappedType);

                    // Check mismatches
                    if ("email".equals(columnName) && dbType != null && dbType.contains("citext") && jdbcTypeCode == null) {
                        mismatchLog.add("Mismatch on " + entity.getName() + ".email: DB is citext but mapped without @JdbcTypeCode");
                    }
                }
            }
        }
        System.out.println("=== SCHEMA MAPPING AUDIT TABLE END ===");

        if (!mismatchLog.isEmpty()) {
            System.out.println("Found Mismatches:");
            mismatchLog.forEach(System.out::println);
        }

        assertThat(mismatchLog).isEmpty();
    }
}
