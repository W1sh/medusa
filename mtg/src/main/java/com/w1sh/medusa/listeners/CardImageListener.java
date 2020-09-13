package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.events.CardImageEvent;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.utils.CardUtils;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class CardImageListener implements CustomEventListener<CardImageEvent> {

    private final CardService cardService;
    private final MessageService messageService;

    @Override
    public Mono<Void> execute(CardImageEvent event) {
        return Mono.just(event)
                .filter(InlineEvent::hasArgument)
                .flatMap(ev -> cardService.getCardByName(ev.getInlineArgument()))
                .flatMap(tuple -> createEmbed(tuple, event))
                .doOnNext(messageService::queue)
                .doAfterTerminate(messageService::flush)
                .then();
    }

    private Mono<Embed> createEmbed(Card card, CardImageEvent event){
        return event.getChannel().map(channel -> {
            if(card.isEmpty() || card.getUri() == null || card.getName() == null || card.getImage() == null || card.getImage().getNormal() == null){
                return CardUtils.createErrorEmbed(channel, event);
            }
            return new Embed(channel, embedCreateSpec -> {
                embedCreateSpec.setColor(Color.GREEN);
                embedCreateSpec.setUrl(card.getUri());
                embedCreateSpec.setTitle(card.getName());
                embedCreateSpec.setImage(card.getImage().getNormal());
            }, event.isFragment(), event.getInlineOrder());
        });
    }
}
