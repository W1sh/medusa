package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
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
public final class CardImageListener implements EventListener<CardImageEvent> {

    private final CardService cardService;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Class<CardImageEvent> getEventType() {
        return CardImageEvent.class;
    }

    @Override
    public Mono<Void> execute(CardImageEvent event) {
        return Mono.just(event)
                .filterWhen(this::validate)
                .flatMap(ev -> Mono.justOrEmpty(ev.getInlineArgument()))
                .flatMap(cardService::getCardByName)
                .flatMap(tuple -> createEmbed(tuple, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Boolean> validate(CardImageEvent event) {
        return Mono.just(event.getInlineArgument() != null && !event.getInlineArgument().isBlank());
    }

    private Mono<Embed> createEmbed(Card card, CardImageEvent event){
        return event.getMessage().getChannel()
                .map(channel -> {
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
