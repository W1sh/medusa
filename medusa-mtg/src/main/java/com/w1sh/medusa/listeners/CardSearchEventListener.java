package com.w1sh.medusa.listeners;

import com.w1sh.medusa.events.CardSearchEvent;
import com.w1sh.medusa.output.ErrorEmbed;
import com.w1sh.medusa.output.SearchEmbed;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class CardSearchEventListener implements CustomEventListener<CardSearchEvent> {

    private final CardService cardService;
    private final MessageService messageService;

    @Override
    public Mono<Void> execute(CardSearchEvent event) {
        if (event.isInvalid()) return messageService.sendOrQueue(event.getChannel(), new ErrorEmbed(event));
        return cardService.getCardsByName(event.getInlineArgument())
                .map(list -> new SearchEmbed(list, event))
                .flatMap(embed -> messageService.sendOrQueue(event.getChannel(), embed))
                .onErrorResume(t -> messageService.sendOrQueue(event.getChannel(), new ErrorEmbed(event)));
    }
}
