package com.w1sh.medusa.listeners;

import com.w1sh.medusa.core.data.Embed;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.events.CardImageEvent;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.utils.CardUtils;
import discord4j.core.object.entity.MessageChannel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.awt.*;

@Component
public class CardImageListener implements EventListener<CardImageEvent> {

    private final CardService cardService;
    private final ResponseDispatcher responseDispatcher;

    public CardImageListener(CommandEventDispatcher eventDispatcher, CardService cardService,
                             ResponseDispatcher responseDispatcher) {
        this.cardService = cardService;
        this.responseDispatcher = responseDispatcher;
        EventFactory.registerEvent(CardImageEvent.INLINE_PREFIX, CardImageEvent.class);
        eventDispatcher.registerListener(this);
    }

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
                .zipWith(event.getMessage().getChannel())
                .map(tuple -> createEmbed(tuple, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Boolean> validate(CardImageEvent event) {
        return Mono.just(event.getInlineArgument() != null && !event.getInlineArgument().isBlank());
    }

    private Embed createEmbed(Tuple2<Card, MessageChannel> tuple, CardImageEvent event){
        Card card = tuple.getT1();
        if(card.isEmpty() || card.getUri() == null || card.getName() == null || card.getImage() == null || card.getImage().getNormal() == null){
            return CardUtils.createErrorEmbed(tuple.getT2(), event);
        }
        return new Embed(tuple.getT2(), embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setUrl(card.getUri());
            embedCreateSpec.setTitle(card.getName());
            embedCreateSpec.setImage(card.getImage().getNormal());
        }, event.isFragment(), event.getInlineOrder());
    }
}
