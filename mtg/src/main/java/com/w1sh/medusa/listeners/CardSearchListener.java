package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.Embed;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.events.CardSearchEvent;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.utils.CardUtils;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public final class CardSearchListener implements EventListener<CardSearchEvent> {

    private final CardService cardService;
    private final ResponseDispatcher responseDispatcher;

    @Override
    public Mono<Void> execute(CardSearchEvent event) {
        return Mono.just(event)
                .filter(InlineEvent::hasArgument)
                .flatMapMany(ev -> cardService.getCardsByName(ev.getInlineArgument()))
                .collectList()
                .flatMap(list -> createEmbed(list, event))
                .doOnNext(responseDispatcher::queue)
                .doAfterTerminate(responseDispatcher::flush)
                .then();
    }

    private Mono<Embed> createEmbed(List<Card> cards, CardSearchEvent event){
        return event.getMessage().getChannel()
                .map(channel -> {
                    if(cards.isEmpty()){
                        return CardUtils.createErrorEmbed(channel, event);
                    }
                    return new Embed(channel, embedCreateSpec -> {
                        embedCreateSpec.setColor(Color.GREEN);
                        embedCreateSpec.setTitle(String.format("Search results for \"%s\"", event.getInlineArgument()));
                        for (int i = 0; i < 5; i++) {
                            embedCreateSpec.addField(String.format("**%d** - **%s**", (i+1), cards.get(i).getName()), cards.get(i).getOracleText(), false);
                        }
                    }, event.isFragment(), event.getInlineOrder());
                });
    }
}
