package com.w1sh.medusa.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.resources.ListResponse;
import com.w1sh.medusa.rest.CardClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.cache.CacheMono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Optional;

@Component
public final class CardService {

    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

    private final CardClient cardClient;
    private final Cache<String, Card> cardCache;

    public CardService(CardClient cardClient) {
        this.cardClient = cardClient;
        this.cardCache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofHours(6))
                .recordStats()
                .build();
    }

    public Mono<Card> getCardByName(String name) {
        return CacheMono.lookup(key -> Mono.justOrEmpty(cardCache.getIfPresent(key))
                .map(Signal::next), name)
                .onCacheMissResume(() -> cardClient.getCardByName(name)
                        .subscribeOn(Schedulers.elastic()))
                .andWriteWith((key, signal) -> Mono.fromRunnable(
                        () -> Optional.ofNullable(signal.get())
                                .ifPresent(value -> cardCache.put(key, value))))
                .onErrorResume(throwable -> {
                    logger.error("Failed to fetch cards with name \"{}\"", name, throwable);
                    return Mono.just(new Card());
                });
    }

    public Flux<Card> getCardsByName(String name) {
        return cardClient.getCardsByName(name)
                .flatMapIterable(ListResponse::getData);
    }
}
