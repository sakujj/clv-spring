package ru.clevertec.house.repository;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import ru.clevertec.house.config.ApplicationConfig;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {ApplicationConfig.class})
public abstract class AbstractDatabaseIntegrationTests {
    private static PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer("postgres:15.4-alpine3.18")
                .withDatabaseName("house_db_test")
                .withUsername("postgres")
                .withPassword("postgres");
        postgres.start();

        var containerDelegate = new JdbcDatabaseDelegate(postgres, "");

        ScriptUtils.runInitScript(containerDelegate, "db/ddl.sql");
        ScriptUtils.runInitScript(containerDelegate, "db/dml.sql");
    }

    @DynamicPropertySource
    static void setJdbcUrl(DynamicPropertyRegistry registry) {
        registry.add("hibernate.connection.url", postgres::getJdbcUrl);
    }
}
