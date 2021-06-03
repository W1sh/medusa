package com.w1sh.medusa.utils;

import com.github.benmanes.caffeine.cache.Cache;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.Optional;
import java.util.function.Supplier;

public final class Caches {

    private Caches(){}

    public static <T, U> Mono<T> lookup(Cache<U, T> cache, U key, Supplier<Mono<T>> onCacheMissSupplier) {
        return CacheMono.lookup(k -> Mono.justOrEmpty(cache.getIfPresent(k))
                .map(Signal::next), key)
                .onCacheMissResume(onCacheMissSupplier)
                .andWriteWith((k, signal) -> Mono.fromRunnable(() ->
                        Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(k, value))));
    }
}
