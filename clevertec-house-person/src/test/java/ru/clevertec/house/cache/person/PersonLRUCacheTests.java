package ru.clevertec.house.cache.person;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class PersonLRUCacheTests extends PersonAbstractCacheTests {

    @DynamicPropertySource
    static void configureCache(DynamicPropertyRegistry registry) {
        registry.add("sakujj.cache.isEnabled", () -> "true");
        registry.add("sakujj.cache.type", () -> "LRU");
        registry.add("sakujj.cache.capacity", () -> 100);
    }
}
