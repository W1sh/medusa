package com.w1sh.medusa.services;

import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.resources.ListResponse;
import com.w1sh.medusa.rest.CardClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Component
public final class CardService {

    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

    private final CardClient cardClient;
    private final MemoryCache<String, Card> cache;

    public CardService(CardClient cardClient) {
        this.cardClient = cardClient;
        this.cache = new MemoryCacheBuilder<String, Card>()
                .maximumSize(10000)
                .expireAfterAccess(Duration.ofHours(6))
                .defaultFetch(key -> cardClient.getCardByName(key)
                        .subscribeOn(Schedulers.elastic()))
                .build();
    }

    public Mono<Card> getCardByName(String name) {
        return cache.get(name)
                .onErrorResume(t -> Mono.fromRunnable(() -> logger.error("Failed to fetch cards with name \"{}\"", name, t)));
    }

    public Flux<Card> getCardsByName(String name) {
        return cardClient.getCardsByName(name)
                .flatMapIterable(ListResponse::getData);
    }
}
