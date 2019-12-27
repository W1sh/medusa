package com.w1sh.medusa.services;

import com.w1sh.medusa.resources.Card;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CardService {

    Mono<Card> getCardByName(String name);

    Flux<Card> getCardsByName(String name);
}
