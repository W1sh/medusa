package com.w1sh.medusa.listeners;

import com.w1sh.medusa.events.CardSearchEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class CardSearchListener implements EventListener<CardSearchEvent> {
    @Override
    public Class<CardSearchEvent> getEventType() {
        return CardSearchEvent.class;
    }

    @Override
    public Mono<Void> execute(CardSearchEvent event) {
        return Mono.empty();
    }
}
