package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.Optional;
import java.util.function.Function;

public class MemoryCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(MemoryCache.class);

    private final Cache<K, V> cache;
    private final Function<K, Mono<V>> defaultFetch;

    public MemoryCache(Cache<K, V> cache, Function<K, Mono<V>> defaultFetch) {
        this.cache = cache;
        this.defaultFetch = defaultFetch;
    }

    protected void put(K key, V value) {
        logger.debug("Caching new value of type {}", value.getClass().getSimpleName());
        cache.put(key, value);
    }

    protected Mono<V> get(K keyProvided){
        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(keyProvided))
                .map(Signal::next), keyProvided)
                .onCacheMissResume(() -> defaultFetch.apply(keyProvided))
                .andWriteWith(this::writeWith);
    }

    protected Mono<Void> writeWith(K key, Signal<? extends V> possibleValue){
        return Mono.fromRunnable(() -> Optional.ofNullable(possibleValue.get()).ifPresent(value -> put(key, value)));
    }
}
