package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

public class MemoryCacheBuilder<K, V> {

    private long maximumSize;
    private int initialCapacity;
    private Duration expireAfterAccess;
    private Function<K, Mono<V>> fetch;

    protected MemoryCacheBuilder<K, V> expireAfterAccess(@NonNull Duration expireAfterAccess){
        this.expireAfterAccess = expireAfterAccess;
        return this;
    }

    protected MemoryCacheBuilder<K, V> maximumSize(long maximumSize){
        this.maximumSize = maximumSize;
        return this;
    }

    protected MemoryCacheBuilder<K, V> initialCapacity(int initialCapacity){
        this.initialCapacity = initialCapacity;
        return this;
    }

    protected MemoryCacheBuilder<K, V> fetch(@NonNull Function<K, Mono<V>> fetch){
        this.fetch = fetch;
        return this;
    }

    protected MemoryCache<K, V> build(){
        final Cache<K, V> cache = Caffeine.newBuilder()
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize)
                .expireAfterAccess(expireAfterAccess)
                .recordStats()
                .build();
        return new MemoryCache<>(cache, fetch);
    }
}
