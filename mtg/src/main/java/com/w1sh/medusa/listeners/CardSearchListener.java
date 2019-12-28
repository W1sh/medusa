package com.w1sh.medusa.listeners;

import com.w1sh.medusa.core.dispatchers.CommandEventDispatcher;
import com.w1sh.medusa.core.events.EventFactory;
import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.events.CardSearchEvent;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.utils.Messenger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.awt.*;

@Component
public class CardSearchListener implements EventListener<CardSearchEvent> {

    private final CardService cardService;

    public CardSearchListener(CommandEventDispatcher eventDispatcher, CardService cardService) {
        this.cardService = cardService;
        EventFactory.registerEvent(CardSearchEvent.INLINE_PREFIX, CardSearchEvent.class);
        eventDispatcher.registerListener(this);
    }

    @Override
    public Class<CardSearchEvent> getEventType() {
        return CardSearchEvent.class;
    }

    @Override
    public Mono<Void> execute(CardSearchEvent event) {
        return Mono.just(event)
                .filterWhen(this::validate)
                .flatMap(ev -> Mono.justOrEmpty(ev.getInlineArgument()))
                .flatMap(cardService::getCardByName)
                .zipWith(event.getMessage().getChannel())
                .flatMap(tuple -> Messenger.send(tuple.getT2(), embedCreateSpec -> {
                    final Card card = tuple.getT1();
                    embedCreateSpec.setColor(Color.GREEN);
                    embedCreateSpec.setUrl(card.getUri());
                    embedCreateSpec.setThumbnail(card.getImage().getSmall());
                    embedCreateSpec.setTitle(String.format("%s %s",
                            card.getName(),
                            card.getManaCost()));
                    embedCreateSpec.addField(String.format("**%s**", card.getTypeLine()),
                            String.format("%s%n*%s*",
                                    card.getOracleText() == null ? Messenger.ZERO_WIDTH_SPACE : card.getOracleText(),
                                    card.getFlavorText() == null ? Messenger.ZERO_WIDTH_SPACE : card.getFlavorText()), false);
                    if(card.getPower() != null || card.getToughness() != null){
                        embedCreateSpec.addField(Messenger.ZERO_WIDTH_SPACE,
                                String.format("**%s/%s**",
                                        card.getPower(),
                                        card.getToughness()), true);
                    }
                }))
                .then();
    }

    private Mono<Boolean> validate(CardSearchEvent event) {
        return Mono.just(event.getInlineArgument() != null && !event.getInlineArgument().isBlank());
    }
}
