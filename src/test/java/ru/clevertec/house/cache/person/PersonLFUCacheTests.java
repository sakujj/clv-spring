package ru.clevertec.house.cache.person;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"enable-cache", "cache-lfu",})
public class PersonLFUCacheTests extends PersonAbstractCacheTests {
}
