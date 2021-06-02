package com.w1sh.medusa.services;

import com.w1sh.medusa.rest.resources.Card;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Supplier;

public interface CardCacheService {

    void put(String name, Card card);

    Mono<Card> getCardByName(String name, Supplier<Mono<Card>> onCacheMissSupplier);

    Mono<List<Card>> getUniquePrintsByName(String name, Supplier<Mono<List<Card>>> onCacheMissSupplier);
}
