package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.events.CardArtworkEvent;
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
public final class CardArtworkEventListener implements CustomEventListener<CardArtworkEvent> {

    private final CardService cardService;
    private final MessageService messageService;
    private final CardUtils cardUtils;

    @Override
    public Mono<Void> execute(CardArtworkEvent event) {
        return cardUtils.validateArgument(event)
                .flatMapMany(cardService::getCardByName)
                .flatMap(card -> createCardArtworkEmbed(card, event))
                .then();
    }

    private Mono<Message> createCardArtworkEmbed(Card card, CardArtworkEvent event){
        if(card.getArtist() == null || card.getImage() == null || card.getImage().getArtwork() == null){
            return cardUtils.createErrorEmbed(event);
        }

        final Consumer<EmbedCreateSpec> specConsumer = embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setImage(card.getImage().getArtwork());
            embedCreateSpec.setFooter("Artwork by: " + card.getArtist(), null);
        };

        final Response response = Response.with(specConsumer, event.getChannel(), event.getChannelId(),
                event.isFragment(), event.getInlineOrder());
        return messageService.sendOrQueue(event.getChannel(), response);
    }
}
