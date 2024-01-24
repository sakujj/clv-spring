package ru.clevertec.house;

import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mapping.model.CamelCaseAbbreviatingFieldNamingStrategy;

@SpringBootApplication(proxyBeanMethods = false)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
//        ImplicitNamingStrategyJpaCompliantImpl
//        PhysicalNamingStrategyStandardImpl
    }
}
