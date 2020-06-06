package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
public class MemoryCache<K, V> {

    private final Cache<K, V> cache;
    private final Function<K, Mono<V>> defaultFetch;

    protected void put(K key, V value) {
        log.debug("Caching new value of type {}", value.getClass().getSimpleName());
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
