package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.Wishlist;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Component
public final class CaffeineWishlistCacheService implements WishlistCacheService {

    private final Cache<String, Wishlist> cache;

    public CaffeineWishlistCacheService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(6))
                .expireAfterWrite(Duration.ofDays(1))
                .build();
    }

    @Override
    public Mono<Wishlist> findByUserId(String userId, Supplier<Mono<Wishlist>> onCacheMissSupplier) {
        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(key))
                .map(Signal::next), userId)
                .onCacheMissResume(onCacheMissSupplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() ->
                        Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(key, value))));
    }
}
