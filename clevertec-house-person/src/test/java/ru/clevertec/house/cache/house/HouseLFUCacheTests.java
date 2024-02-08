package ru.clevertec.house.cache.house;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;


public class HouseLFUCacheTests extends HouseAbstractCacheTests {

    @DynamicPropertySource
    static void configureCache(DynamicPropertyRegistry registry) {
        registry.add("sakujj.cache.isEnabled", () -> "true");
        registry.add("sakujj.cache.type", () -> "LFU");
        registry.add("sakujj.cache.capacity", () -> 100);
    }
}
