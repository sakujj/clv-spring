package ru.clevertec.house.cache.house;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({ "enable-cache", "cache-lru"})
public class HouseLRUCacheTestsHouse extends HouseAbstractCacheTests {
}
