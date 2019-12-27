package com.w1sh.medusa.services.impl;

import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.resources.ListResponse;
import com.w1sh.medusa.rest.CardClient;
import com.w1sh.medusa.services.CardService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CardServiceImpl implements CardService {

    private final CardClient cardClient;

    public CardServiceImpl(CardClient cardClient) {
        this.cardClient = cardClient;
    }

    @Override
    public Mono<Card> getCardByName(String name) {
        return cardClient.getCardByName(name);
    }

    @Override
    public Flux<Card> getCardsByName(String name) {
        return cardClient.getCardsByName(name)
                .flatMapIterable(ListResponse::getData);
    }
}
