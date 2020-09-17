package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.events.CardDetailEvent;
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

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public final class CardDetailEventListener implements CustomEventListener<CardDetailEvent> {

    private final CardService cardService;
    private final MessageService messageService;
    private final CardUtils cardUtils;

    @Override
    public Mono<Void> execute(CardDetailEvent event) {
        return cardUtils.validateArgument(event)
                .flatMapMany(cardService::getCardByName)
                .defaultIfEmpty(new Card())
                .flatMap(card -> createCardDetailEmbed(card, event))
                .then();
    }

    private Mono<Message> createCardDetailEmbed(Card card, CardDetailEvent event) {
        if (card.isEmpty() || card.getImage() == null || card.getImage().getSmall() == null || card.getUri() == null
                || card.getName() == null || card.getManaCost() == null || card.getTypeLine() == null) {
            return cardUtils.createErrorEmbed(event);
        }

        final Consumer<EmbedCreateSpec> specConsumer = embedCreateSpec -> {
            embedCreateSpec.setThumbnail(card.getImage().getSmall());
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setUrl(card.getUri());
            embedCreateSpec.setTitle(card.getName());
            embedCreateSpec.addField(String.format("**%s**", card.getTypeLine()),
                    String.format("%s%n*%s*",
                            card.getOracleText() == null ? MessageService.ZERO_WIDTH_SPACE : card.getOracleText(),
                            card.getFlavorText() == null ? MessageService.ZERO_WIDTH_SPACE : card.getFlavorText()), false);
            if (card.getPower() != null || card.getToughness() != null) {
                embedCreateSpec.addField(MessageService.ZERO_WIDTH_SPACE,
                        String.format("**%s/%s**",
                                card.getPower(),
                                card.getToughness()), true);
            }
        };

        final Response response = Response.with(specConsumer, event.getChannel(), event.getChannelId(),
                event.isFragment(), event.getInlineOrder());
        return messageService.sendOrQueue(event.getChannel(), response);
    }
}
