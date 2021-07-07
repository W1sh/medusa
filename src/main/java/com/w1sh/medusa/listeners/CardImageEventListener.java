package com.w1sh.medusa.listeners;

import com.w1sh.medusa.events.CardImageEvent;
import com.w1sh.medusa.output.ErrorEmbed;
import com.w1sh.medusa.output.ImageEmbed;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.services.MessageService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class CardImageEventListener implements CustomEventListener<CardImageEvent> {

    private final CardService cardService;
    private final MessageService messageService;

    public CardImageEventListener(CardService cardService, MessageService messageService) {
        this.cardService = cardService;
        this.messageService = messageService;
    }

    @Override
    public Mono<Void> execute(CardImageEvent event) {
        if (event.isInvalid()) return messageService.sendOrQueue(event.getChannel(), new ErrorEmbed(event));
        return cardService.getCardByName(event.getInlineArgument())
                .map(card -> new ImageEmbed(card, event))
                .flatMap(embed -> messageService.sendOrQueue(event.getChannel(), embed))
                .onErrorResume(t -> messageService.sendOrQueue(event.getChannel(), new ErrorEmbed(event)));
    }
}
