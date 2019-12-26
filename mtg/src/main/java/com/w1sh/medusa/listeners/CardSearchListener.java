package com.w1sh.medusa.listeners;

import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.MultipleArgsEventListener;
import com.w1sh.medusa.events.CardSearchEvent;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CardSearchListener implements MultipleArgsEventListener<CardSearchEvent> {

    public CardSearchListener(CommandEventDispatcher eventDispatcher) {
        EventFactory.registerEvent(CardSearchEvent.KEYWORD, CardSearchEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<CardSearchEvent> getEventType() {
        return CardSearchEvent.class;
    }

    @Override
    public Mono<Void> execute(CardSearchEvent event) {
        return Mono.just(event)
                .filterWhen(this::validate)
                .flatMap(ev -> Mono.justOrEmpty(ev.getMessage().getContent()))
                .map(content -> content.split(" ")[1])
                //.map(cardClient::getCardByName)
                .doOnNext(s -> Messenger.send(event, s).subscribe())
                .then();
    }

    @Override
    public Mono<Boolean> validate(CardSearchEvent event) {
        return Mono.just(true);
    }
}