package com.classicstore.common;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresTestContainer {

    private static PostgreSQLContainer<?> container;

    public static synchronized PostgreSQLContainer<?> getInstance() {
        if (container == null) {
            container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("classicstore")
                    .withUsername("postgres")
                    .withPassword("postgres");
            try {
                container.start();
            } catch (Exception e) {
                // If Docker is not available in the environment, fallback gracefully or wrap exception
                throw e;
            }
        }
        return container;
    }
}
