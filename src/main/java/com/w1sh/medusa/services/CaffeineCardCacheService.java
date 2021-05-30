package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.resources.Card;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Component
public final class CaffeineCardCacheService implements CardCacheService {

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
        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(key))
                .map(Signal::next), name)
                .onCacheMissResume(onCacheMissSupplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() ->
                        Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(key, value))));
    }

    @Override
    public Mono<List<Card>> getUniquePrintsByName(String name, Supplier<Mono<List<Card>>> onCacheMissSupplier) {
        return CacheMono.lookup(key -> Mono.justOrEmpty(uniquePrints.getIfPresent(key))
                .map(Signal::next), name)
                .onCacheMissResume(onCacheMissSupplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() ->
                        Optional.ofNullable(signal.get()).ifPresent(value -> uniquePrints.put(key, value))));
    }
}
