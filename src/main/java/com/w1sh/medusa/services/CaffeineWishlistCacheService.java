package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.data.Wishlist;
import com.w1sh.medusa.utils.Caches;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Supplier;

@Component
public final class CaffeineWishlistCacheService implements WishlistCacheService {

    private static final Logger log = LoggerFactory.getLogger(CaffeineWishlistCacheService.class);
    private final Cache<String, Wishlist> cache;

    public CaffeineWishlistCacheService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(6))
                .expireAfterWrite(Duration.ofDays(1))
                .build();
    }

    @Override
    public void put(String userId, Wishlist wishlist) {
        cache.put(userId, wishlist);
    }

    @Override
    public Mono<Wishlist> findByUserId(String userId, Supplier<Mono<Wishlist>> onCacheMissSupplier) {
        return Caches.lookup(cache, userId, onCacheMissSupplier);
    }
}
