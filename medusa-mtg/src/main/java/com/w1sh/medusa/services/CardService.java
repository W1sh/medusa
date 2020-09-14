package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.resources.ListResponse;
import com.w1sh.medusa.rest.CardClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.cache.CacheMono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
@Slf4j
public final class CardService {

    private final CardClient cardClient;
    private final Cache<String, Card> cache;
    private final Cache<String, List<Card>> uniquePrints;

    public CardService(CardClient cardClient) {
        this.cardClient = cardClient;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(6))
                .expireAfterWrite(Duration.ofDays(1))
                .build();

        this.uniquePrints = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(2))
                .expireAfterWrite(Duration.ofHours(6))
                .build();
    }

    public Mono<Card> getCardByName(String name) {
        final Supplier<Mono<Card>> supplier = () -> cardClient.getCardByName(name);

        return CacheMono.lookup(key -> Mono.justOrEmpty(cache.getIfPresent(key))
                .map(Signal::next), name)
                .onCacheMissResume(supplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> cache.put(key, value))))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to fetch card with name \"{}\"", name, t)));
    }

    public Flux<Card> getCardsByName(String name) {
        return cardClient.getCardsByName(name)
                .doOnNext(response -> log.info("Retrieved {} prints for \"{}\"", response.getTotalCards(), name))
                .flatMapIterable(ListResponse::getData)
                .doOnNext(card -> cache.put(card.getName(), card))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to fetch cards with name \"{}\"", name, t)));
    }

    public Mono<List<Card>> getUniquePrintsByName(String name) {
        final Supplier<Mono<List<Card>>> supplier = () -> cardClient.getCardByName(name)
                .filter(card -> !StringUtils.isEmpty(card.getUniquePrintsUri()))
                .flatMap(card -> cardClient.getUniquePrints(card.getUniquePrintsUri()))
                .filter(response -> !response.getData().isEmpty())
                .doOnNext(response -> log.info("Retrieved {} unique prints for {}", response.getTotalCards(), response.getData().get(0).getName()))
                .map(ListResponse::getData);

        return CacheMono.lookup(key -> Mono.justOrEmpty(uniquePrints.getIfPresent(key))
                .map(Signal::next), name)
                .onCacheMissResume(supplier)
                .andWriteWith((key, signal) -> Mono.fromRunnable(() -> Optional.ofNullable(signal.get()).ifPresent(value -> uniquePrints.put(key, value))))
                .onErrorResume(t -> Mono.fromRunnable(() -> log.error("Failed to fetch cards with name \"{}\"", name, t)));
    }
}
