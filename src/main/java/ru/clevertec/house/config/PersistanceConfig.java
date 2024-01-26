package ru.clevertec.house.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.clevertec.house.constant.ApplicationConstants;

@Configuration
@EnableJpaRepositories(basePackages = ApplicationConstants.DEFAULT_JPA_REPOSITORIES_BASE_PACKAGE)
public class PersistanceConfig {
}
