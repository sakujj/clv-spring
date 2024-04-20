package ru.clevertec.house.testcontainer;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@Testcontainers
public class ExclusivePostgresContainerInitializer {

    private static final String DOCKER_CONTAINER_IMAGE = "postgres:15.4-alpine3.18";
    private static final String DB_NAME = "house_db_test";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "postgres";

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer(DOCKER_CONTAINER_IMAGE)
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USERNAME)
            .withPassword(DB_PASSWORD);

    @DynamicPropertySource
    static void setJdbcUrl(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }
}
