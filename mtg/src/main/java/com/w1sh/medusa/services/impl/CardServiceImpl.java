package com.w1sh.medusa.services.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.resources.ListResponse;
import com.w1sh.medusa.rest.CardClient;
import com.w1sh.medusa.services.CardService;
import org.springframework.stereotype.Component;
import reactor.cache.CacheMono;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Component
public class CardServiceImpl implements CardService {

    private final CardClient cardClient;
    private final Cache<String, Card> cardCache = Caffeine.newBuilder().build();

    public CardServiceImpl(CardClient cardClient) {
        this.cardClient = cardClient;
    }

    @Override
    public Mono<Card> getCardByName(String name) {
        return CacheMono.lookup(key -> Mono.justOrEmpty(cardCache.getIfPresent(key))
                .map(Signal::next), name)
                .onCacheMissResume(
                        () -> cardClient.getCardByName(name)
                                .subscribeOn(Schedulers.elastic()))
                .andWriteWith(
                        (key, signal) -> Mono.fromRunnable(
                                () -> Optional.ofNullable(signal.get())
                                        .ifPresent(value -> cardCache.put(key, value))));
        /*return cardClient.getCardByName(name);*/
    }

    @Override
    public Flux<Card> getCardsByName(String name) {
        return cardClient.getCardsByName(name)
                .flatMapIterable(ListResponse::getData);
    }
}
