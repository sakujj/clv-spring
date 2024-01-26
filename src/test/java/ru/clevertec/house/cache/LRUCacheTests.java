package ru.clevertec.house.cache;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "test-lru"})
@SpringBootTest
public class LRUCacheTests extends AbstractCacheTests {
}
