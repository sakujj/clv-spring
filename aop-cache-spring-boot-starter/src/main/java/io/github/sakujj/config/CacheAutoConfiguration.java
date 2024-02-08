package io.github.sakujj.config;

import io.github.sakujj.cache.Cache;
import io.github.sakujj.cache.LFUCache;
import io.github.sakujj.cache.LRUCache;
import io.github.sakujj.cache.aop.CacheAspect;
import io.github.sakujj.configprops.CacheProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(value = CacheProperties.class)
@ConditionalOnProperty(name = "sakujj.cache.isEnabled", matchIfMissing = true)
public class CacheAutoConfiguration {

    private static final String DEFAULT_CACHE_TYPE = "LRU";
    private static final int DEFAULT_CACHE_CAPACITY = 100;

    public static final String PREFIX = "sakujj.cache";

    @Autowired
    private CacheProperties cacheProperties;

    @Bean
    @ConditionalOnMissingBean
    public Cache cache() {
        return inferCacheFromProperties(cacheProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheAspect cacheAspect(Cache cache) {
        return new CacheAspect(cache);
    }

    private static Cache inferCacheFromProperties(CacheProperties cacheProperties) {

        Integer inferredCapacity = cacheProperties.getCapacity();
        int capacity = inferredCapacity != null && inferredCapacity > 0
                ? inferredCapacity
                : DEFAULT_CACHE_CAPACITY;


        String inferredType = cacheProperties.getType();

        String type = inferredType != null
                ? inferredType
                : DEFAULT_CACHE_TYPE;

        if (!type.equals("LFU") && !type.equals("LRU")) {
            type = DEFAULT_CACHE_TYPE;
        }

        return switch (type) {
            case "LRU" -> new LRUCache(capacity);
            case "LFU" -> new LFUCache(capacity);
            default -> throw new RuntimeException("Internal error : wrong cache type");
        };
    }

}

