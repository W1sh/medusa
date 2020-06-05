package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

public class MemoryCacheBuilder<K, V> {

    private long maximumSize;
    private Duration expireAfterAccess;
    private Function<K, Mono<V>> defaultFetch;

    protected MemoryCacheBuilder<K, V> expireAfterAccess(@NonNull Duration expireAfterAccess){
        this.expireAfterAccess = expireAfterAccess;
        return this;
    }

    protected MemoryCacheBuilder<K, V> maximumSize(long maximumSize){
        this.maximumSize = maximumSize;
        return this;
    }

    protected MemoryCacheBuilder<K, V> defaultFetch(@NonNull Function<K, Mono<V>> defaultFetch){
        this.defaultFetch = defaultFetch;
        return this;
    }

    protected MemoryCache<K, V> build(){
        final Cache<K, V> cache = Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterAccess(expireAfterAccess)
                .recordStats()
                .build();
        return new MemoryCache<>(cache, defaultFetch);
    }
}
