package ru.clevertec.house.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.clevertec.house.cache.Cache;
import ru.clevertec.house.cache.LFUCache;
import ru.clevertec.house.cache.LRUCache;
import ru.clevertec.house.cache.NoOpCache;

@Configuration(proxyBeanMethods = false)
public class CacheConfig {

    @Bean
    @Profile("!test")
    public Cache cache() {
        return new LFUCache(100);
    }

    @Bean
    @Profile("test-no-cache")
    public Cache cacheNoOp(){
        return new NoOpCache();
    }

    @Bean
    @Profile("test-lfu")
    public Cache cacheLFU() {
        return new LFUCache(100);
    }

    @Bean
    @Profile("test-lru")
    public Cache cacheLRU() {
        return new LRUCache(100);
    }
}
