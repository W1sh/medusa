package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.rest.resources.Card;
import com.w1sh.medusa.utils.Caches;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Component
public final class CaffeineCardCacheService implements CardCacheService {

    private static final Logger log = LoggerFactory.getLogger(CaffeineCardCacheService.class);
    private final Cache<String, Card> cache;
    private final Cache<String, List<Card>> uniquePrints;

    public CaffeineCardCacheService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(6))
                .expireAfterWrite(Duration.ofDays(1))
                .build();
        this.uniquePrints = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(2))
                .expireAfterWrite(Duration.ofHours(6))
                .build();
    }

    @Override
    public void put(String name, Card card) {
        cache.put(card.getName(), card);
    }

    @Override
    public Mono<Card> getCardByName(String name, Supplier<Mono<Card>> onCacheMissSupplier) {
        return Caches.lookup(cache, name, onCacheMissSupplier);
    }

    @Override
    public Mono<List<Card>> getUniquePrintsByName(String name, Supplier<Mono<List<Card>>> onCacheMissSupplier) {
        return Caches.lookup(uniquePrints, name, onCacheMissSupplier);
    }
}
