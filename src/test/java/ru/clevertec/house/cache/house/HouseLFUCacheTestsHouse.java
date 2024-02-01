package ru.clevertec.house.cache.house;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"enable-cache", "cache-lfu",})
public class HouseLFUCacheTestsHouse extends HouseAbstractCacheTests {
}
