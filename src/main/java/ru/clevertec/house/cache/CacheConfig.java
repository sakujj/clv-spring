package ru.clevertec.house.cache;

import lombok.Getter;


@Getter
public class CacheConfig {
    public enum CacheType {
        LRU, LFU, NONE
    }

    private static final CacheType DEFAULT_TYPE = CacheType.NONE;
    private static final int DEFAULT_CAPACITY = 0;

    private final CacheType type;
    private final int capacity;

    public CacheConfig() {
        type = DEFAULT_TYPE;
        capacity = DEFAULT_CAPACITY;
    }

    public CacheConfig(CacheType type, int capacity) {
        this.type = type;
        this.capacity = capacity;
    }
}
