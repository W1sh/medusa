package com.w1sh.medusa.listeners;

import com.w1sh.medusa.events.CardArtworkEvent;
import com.w1sh.medusa.output.ArtworkEmbed;
import com.w1sh.medusa.output.ErrorEmbed;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class CardArtworkEventListener implements CustomEventListener<CardArtworkEvent> {

    private final CardService cardService;
    private final MessageService messageService;

    @Override
    public Mono<Void> execute(CardArtworkEvent event) {
        if (event.isInvalid()) return messageService.sendOrQueue(event.getChannel(), new ErrorEmbed(event)).then();
        return cardService.getCardByName(event.getInlineArgument())
                .map(card -> new ArtworkEmbed(card, event))
                .flatMap(embed -> messageService.sendOrQueue(event.getChannel(), embed))
                .switchIfEmpty(messageService.sendOrQueue(event.getChannel(), new ErrorEmbed(event)))
                .then();
    }
}
