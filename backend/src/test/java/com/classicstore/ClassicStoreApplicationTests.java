package com.classicstore;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ClassicStoreApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("jwt.secret", () -> "super-secret-jwt-key-for-testing-must-be-at-least-32-bytes-long");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment environment;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        // Application context loads successfully
    }

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void ddlAutoValidateIsEffective() {
        String resolvedDdlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");
        assertThat(resolvedDdlAuto).isEqualTo("validate");

        Object hbm2ddlAutoProp = entityManagerFactory.getProperties().get("hibernate.hbm2ddl.auto");
        assertThat(hbm2ddlAutoProp).isNotNull();
        assertThat(hbm2ddlAutoProp.toString()).isEqualTo("validate");
    }

    @Test
    void defaultProfileStartupRunsFlywayWith7SuccessfulMigrations() {
        Integer successCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM store.flyway_schema_history WHERE type = 'SQL' AND success = true",
                Integer.class
        );
        assertThat(successCount).isNotNull();
        assertThat(successCount).isEqualTo(7);
    }
}
