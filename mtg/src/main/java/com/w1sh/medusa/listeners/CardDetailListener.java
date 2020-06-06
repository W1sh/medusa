package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.CardDetailEvent;
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
public final class CardDetailListener implements EventListener<CardDetailEvent> {

    private final CardService cardService;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Class<CardDetailEvent> getEventType() {
        return CardDetailEvent.class;
    }

    @Override
    public Mono<Void> execute(CardDetailEvent event) {
        return Mono.just(event)
                .filterWhen(this::validate)
                .flatMap(ev -> Mono.justOrEmpty(ev.getInlineArgument()))
                .flatMap(cardService::getCardByName)
                .flatMap(tuple -> this.createEmbed(tuple, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Boolean> validate(CardDetailEvent event) {
        return Mono.just(event.getInlineArgument() != null && !event.getInlineArgument().isBlank());
    }

    private Mono<Embed> createEmbed(Card card, CardDetailEvent event){
        return event.getMessage().getChannel()
                .map(channel -> {
                    if(card.isEmpty() || card.getImage() == null || card.getImage().getSmall() == null || card.getUri() == null
                            || card.getName() == null || card.getManaCost() == null || card.getTypeLine() == null){
                        return CardUtils.createErrorEmbed(channel, event);
                    }
                    return new Embed(channel, embedCreateSpec -> {
                        embedCreateSpec.setThumbnail(card.getImage().getSmall());
                        embedCreateSpec.setColor(Color.GREEN);
                        embedCreateSpec.setUrl(card.getUri());
                        embedCreateSpec.setTitle(String.format("%s %s",
                                card.getName(),
                                card.getManaCost()));
                        embedCreateSpec.addField(String.format("**%s**", card.getTypeLine()),
                                String.format("%s%n*%s*",
                                        card.getOracleText() == null ? ResponseUtils.ZERO_WIDTH_SPACE : card.getOracleText(),
                                        card.getFlavorText() == null ? ResponseUtils.ZERO_WIDTH_SPACE : card.getFlavorText()), false);
                        if(card.getPower() != null || card.getToughness() != null){
                            embedCreateSpec.addField(ResponseUtils.ZERO_WIDTH_SPACE,
                                    String.format("**%s/%s**",
                                            card.getPower(),
                                            card.getToughness()), true);
                        }
                    }, event.isFragment(), event.getInlineOrder());
                });
    }
}
