package com.w1sh.medusa.listeners;

import com.w1sh.medusa.events.CardDetailEvent;
import com.w1sh.medusa.output.DetailsEmbed;
import com.w1sh.medusa.output.ErrorEmbed;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.services.MessageService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class CardDetailEventListener implements CustomEventListener<CardDetailEvent> {

    private final CardService cardService;
    private final MessageService messageService;

    public CardDetailEventListener(CardService cardService, MessageService messageService) {
        this.cardService = cardService;
        this.messageService = messageService;
    }

    @Override
    public Mono<Void> execute(CardDetailEvent event) {
        if (event.isInvalid()) return messageService.sendOrQueue(event.getChannel(), new ErrorEmbed(event));
        return cardService.getCardByName(event.getInlineArgument())
                .map(card -> new DetailsEmbed(card, event))
                .flatMap(embed -> messageService.sendOrQueue(event.getChannel(), embed))
                .onErrorResume(t -> messageService.sendOrQueue(event.getChannel(), new ErrorEmbed(event)));
    }
}
