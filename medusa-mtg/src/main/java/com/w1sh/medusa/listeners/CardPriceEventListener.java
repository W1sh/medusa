package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.CardPriceEvent;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.utils.CardUtils;
import com.w1sh.medusa.utils.ResponseUtils;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class CardPriceEventListener implements EventListener<CardPriceEvent> {

    private final CardService cardService;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Mono<Void> execute(CardPriceEvent event) {
        return Mono.just(event)
                .filter(InlineEvent::hasArgument)
                .flatMap(ev -> cardService.getCardByName(ev.getInlineArgument()))
                .flatMap(tuple -> this.createEmbed(tuple, event))
                .switchIfEmpty(notFoundMessage(event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Response> createEmbed(Card card, CardPriceEvent event){
        return event.getMessage().getChannel()
                .map(channel -> {
                    if(card.isEmpty() || card.getUri() == null || card.getName() == null
                            || card.getPrice() == null) {
                        return CardUtils.createErrorEmbed(channel, event);
                    }
                    return new Embed(channel, embedCreateSpec -> {
                        embedCreateSpec.setColor(Color.GREEN);
                        embedCreateSpec.setUrl(card.getUri());
                        embedCreateSpec.setTitle(String.format("**Prices for %s**", card.getName()));
                        embedCreateSpec.setDescription(card.getTypeLine());
                        embedCreateSpec.addField(String.format("**%s**", card.getSet()), String.format("$%s %s €%s",
                                card.getPrice().getUsd(), ResponseUtils.BULLET, card.getPrice().getUsd()), true);
                    }, event.isFragment(), event.getInlineOrder());
                });
    }

    private Mono<Response> notFoundMessage(CardPriceEvent event) {
        return event.getMessage().getChannel()
                .map(channel -> CardUtils.createErrorEmbed(channel, event));
    }
}
