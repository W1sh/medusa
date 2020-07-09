package com.w1sh.medusa.services.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

@Slf4j
public class MemoryCacheBuilder<K, V> {

    private long maximumSize;
    private int initialCapacity;
    private Duration expireAfterAccess;
    private Function<K, Mono<V>> fetch;

    public MemoryCacheBuilder<K, V> expireAfterAccess(@NonNull Duration expireAfterAccess){
        this.expireAfterAccess = expireAfterAccess;
        return this;
    }

    public MemoryCacheBuilder<K, V> maximumSize(long maximumSize){
        this.maximumSize = maximumSize;
        return this;
    }

    public MemoryCacheBuilder<K, V> initialCapacity(int initialCapacity){
        this.initialCapacity = initialCapacity;
        return this;
    }

    public MemoryCacheBuilder<K, V> fetch(@NonNull Function<K, Mono<V>> fetch){
        this.fetch = fetch;
        return this;
    }

    public MemoryCache<K, V> build(){
        this.maximumSize = defaultIf(maximumSize <= 0, "No value for 'maximumSize' was provided, default value of {} will be used",
                maximumSize, 10000L);
        this.initialCapacity = defaultIf(initialCapacity <= 0, "No value for 'initialCapacity' was provided, default value of {} will be used",
                initialCapacity, 1000);
        this.expireAfterAccess = defaultIf(expireAfterAccess != null, "No value for 'expireAfterAccess' was provided, default value of {} will be used",
                expireAfterAccess, Duration.ofHours(24));

        final Cache<K, V> cache = Caffeine.newBuilder()
                .initialCapacity(initialCapacity)
                .maximumSize(maximumSize)
                .expireAfterAccess(expireAfterAccess)
                .recordStats()
                .build();
        return new MemoryCache<>(cache, fetch);
    }

    private <T> T defaultIf(boolean evaluation, String logMessage, T value, T defaultValue){
        if (evaluation) {
            return value;
        } else {
            log.warn(logMessage, defaultValue);
            return defaultValue;
        }
    }
}
