package com.w1sh.medusa.listeners;

import com.w1sh.medusa.core.data.Embed;
import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.events.CardImageEvent;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.services.CardService;
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
                .map(tuple -> createEmbed(tuple, event.isFragment(), event.getInlineOrder()))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Boolean> validate(CardImageEvent event) {
        return Mono.just(event.getInlineArgument() != null && !event.getInlineArgument().isBlank());
    }

    private Embed createEmbed(Tuple2<Card, MessageChannel> tuple, Boolean isFragment, Integer order){
        return new Embed(tuple.getT2(), embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setUrl(tuple.getT1().getUri());
            embedCreateSpec.setTitle(tuple.getT1().getName());
            if(tuple.getT1().getImage() != null && tuple.getT1().getImage().getNormal() != null){
                embedCreateSpec.setImage(tuple.getT1().getImage().getNormal());
            }
        }, isFragment, order);
    }
}
