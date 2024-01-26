package ru.clevertec.house.cache;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"test", "test-lfu"})
@SpringBootTest
public class LFUCacheTests extends AbstractCacheTests {
}
