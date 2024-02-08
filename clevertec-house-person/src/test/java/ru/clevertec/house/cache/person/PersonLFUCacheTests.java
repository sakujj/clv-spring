package ru.clevertec.house.cache.person;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class PersonLFUCacheTests extends PersonAbstractCacheTests {

    @DynamicPropertySource
    static void configureCache(DynamicPropertyRegistry registry) {
        registry.add("sakujj.cache.isEnabled", () -> "true");
        registry.add("sakujj.cache.type", () -> "LFU");
        registry.add("sakujj.cache.capacity", () -> 100);
    }
}
