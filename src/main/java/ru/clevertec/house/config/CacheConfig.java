package ru.clevertec.house.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.clevertec.house.cache.Cache;
import ru.clevertec.house.cache.LFUCache;
import ru.clevertec.house.cache.LRUCache;
import ru.clevertec.house.cache.aop.CacheAspect;

@Configuration(proxyBeanMethods = false)
@Profile("enable-cache")
public class CacheConfig {

    @Bean
    public CacheAspect cacheAspect(Cache cache) {
        return new CacheAspect(cache);
    }

    @Bean
    @Profile("!test")
    public Cache cache() {
        return new LFUCache(100);
    }

    @Bean
    @Profile({"cache-lfu & test"})
    public Cache cacheLFU() {
        return new LFUCache(100);
    }

    @Bean
    @Profile({"cache-lru & test"})
    public Cache cacheLRU() {
        return new LRUCache(100);
    }
}
