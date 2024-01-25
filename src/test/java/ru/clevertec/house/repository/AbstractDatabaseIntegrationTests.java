package ru.clevertec.house.repository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles("test")
public abstract class AbstractDatabaseIntegrationTests {

    private static final PostgreSQLContainer<?> postgres;

    private static final String DOCKER_CONTAINER_IMAGE = "postgres:15.4-alpine3.18";
    private static final String DB_NAME = "house_db_test";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "postgres";

    static {
        postgres = new PostgreSQLContainer(DOCKER_CONTAINER_IMAGE)
                .withDatabaseName(DB_NAME)
                .withUsername(DB_USERNAME)
                .withPassword(DB_PASSWORD);
        postgres.start();
    }

    @DynamicPropertySource
    static void setJdbcUrl(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }
}
