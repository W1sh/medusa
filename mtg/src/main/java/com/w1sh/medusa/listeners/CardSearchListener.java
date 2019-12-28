package com.w1sh.medusa.listeners;

import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.events.CardSearchEvent;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class CardSearchListener implements EventListener<CardSearchEvent> {

    private final Pattern pattern = Pattern.compile("\\{\\{.+?(?:}})");
    private final CardService cardService;

    public CardSearchListener(CommandEventDispatcher eventDispatcher, CardService cardService) {
        this.cardService = cardService;
        EventFactory.registerEvent(CardSearchEvent.INLINE_PREFIX, CardSearchEvent.class);
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
                .flatMapIterable(this::findAllMatches)
                .flatMap(cardService::getCardByName)
                .flatMap(s -> Messenger.send(event, s.getName()))
                .then();
    }

    public Mono<Boolean> validate(CardSearchEvent event) {
        return Mono.just(true);
    }

    private List<String> findAllMatches(String message){
        Matcher matcher = pattern.matcher(message);
        return matcher.results()
                .map(matchResult -> matchResult.group().replaceAll("[{}]", ""))
                .collect(Collectors.toList());
    }
}
