package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.events.CardPriceEvent;
import com.w1sh.medusa.resources.Card;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.utils.CardUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public final class CardPriceEventListener implements CustomEventListener<CardPriceEvent> {

    private final CardService cardService;
    private final MessageService messageService;
    private final CardUtils cardUtils;

    @Override
    public Mono<Void> execute(CardPriceEvent event) {
        return Mono.just(event)
                .filter(InlineEvent::hasArgument)
                .flatMap(ev -> cardService.getUniquePrintsByName(ev.getInlineArgument()))
                .flatMap(list -> createCardPriceEmbed(list, event))
                .switchIfEmpty(cardUtils.createErrorEmbed(event))
                .then();
    }

    private Mono<Message> createCardPriceEmbed(List<Card> list, CardPriceEvent event) {
        if(list.isEmpty()) return cardUtils.createErrorEmbed(event);

        final Consumer<EmbedCreateSpec> specConsumer = embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setTitle(String.format("**Prices for %s**", list.get(0).getName()));
            embedCreateSpec.setDescription(list.get(0).getTypeLine());

            final int maxFields = Math.min(list.size(), 4);
            if (maxFields % 2 == 0) {
                addFieldsForEvenPrints(embedCreateSpec, list, maxFields);
            } else {
                addFieldsForOddPrints(embedCreateSpec, list, maxFields);
            }
        };

        final Response response = Response.with(specConsumer, event.getChannel(), event.getChannelId(),
                event.isFragment(), event.getInlineOrder());
        return messageService.sendOrQueue(event.getChannel(), response);
    }

    private void addFieldsForEvenPrints(EmbedCreateSpec embedCreateSpec, List<Card> list, int maxFields) {
        for (int i = 0; i < maxFields; i += 2) {
            embedCreateSpec.addField(String.format("**%s**", list.get(i).getSet()), String.format("%s %s %s",
                    getUsdField(list.get(i)), MessageService.BULLET, getEurField(list.get(i))), true);
            embedCreateSpec.addField(MessageService.ZERO_WIDTH_SPACE, MessageService.ZERO_WIDTH_SPACE, true);
            embedCreateSpec.addField(String.format("**%s**", list.get(i + 1).getSet()), String.format("%s %s %s",
                    getUsdField(list.get(i + 1)), MessageService.BULLET, getEurField(list.get(i + 1))), true);
        }
    }

    private void addFieldsForOddPrints(EmbedCreateSpec embedCreateSpec, List<Card> list, int maxFields) {
        for (int i = 0; i < maxFields; i++) {
            embedCreateSpec.addField(String.format("**%s**", list.get(i).getSet()), String.format("%s %s %s",
                    getUsdField(list.get(i)), MessageService.BULLET, getEurField(list.get(i))), true);
        }
    }

    private String getUsdField(Card card){
        return StringUtils.isEmpty(card.getPrice().getUsd()) ? "N/A" : String.format("$%s", card.getPrice().getUsd());
    }

    private String getEurField(Card card){
        return StringUtils.isEmpty(card.getPrice().getEur()) ? "N/A" : String.format("€%s", card.getPrice().getEur());
    }
}
