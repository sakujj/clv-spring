package ru.clevertec.house.cache.person;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"enable-cache", "cache-lru"})
public class PersonLRUCacheTestsHouse extends PersonAbstractCacheTests {
}
