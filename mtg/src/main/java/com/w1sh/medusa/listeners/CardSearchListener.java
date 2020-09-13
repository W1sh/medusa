package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.events.CardSearchEvent;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.utils.CardUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public final class CardSearchListener implements CustomEventListener<CardSearchEvent> {

    private final CardService cardService;
    private final MessageService messageService;
    private final CardUtils cardUtils;

    @Override
    public Mono<Void> execute(CardSearchEvent event) {
        return Mono.just(event)
                .filter(InlineEvent::hasArgument)
                .flatMapMany(ev -> cardService.getCardsByName(ev.getInlineArgument()))
                .collectList()
                .flatMap(list -> createCardSearchEmbed(list, event))
                .then();
    }

    private Mono<Message> createCardSearchEmbed(List<Card> cards, CardSearchEvent event){
        if(cards.isEmpty()){
            return cardUtils.createErrorEmbed(event);
        }

        final Consumer<EmbedCreateSpec> specConsumer = embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setTitle(String.format("Search results for \"%s\"", event.getInlineArgument()));
            for (int i = 0; i < 5; i++) {
                embedCreateSpec.addField(String.format("**%d** - **%s**", (i+1), cards.get(i).getName()), cards.get(i).getOracleText(), false);
            }
        };

        final Response response = Response.with(specConsumer, event.getChannel(), event.getChannelId(),
                event.isFragment(), event.getInlineOrder());
        return messageService.sendOrQueue(event.getChannel(), response);
    }
}
