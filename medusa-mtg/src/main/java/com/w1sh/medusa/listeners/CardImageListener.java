package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.events.CardImageEvent;
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
public final class CardImageListener implements CustomEventListener<CardImageEvent> {

    private final CardService cardService;
    private final MessageService messageService;
    private final CardUtils cardUtils;

    @Override
    public Mono<Void> execute(CardImageEvent event) {
        return Mono.just(event)
                .filter(InlineEvent::hasArgument)
                .flatMap(ev -> cardService.getCardByName(ev.getInlineArgument()))
                .flatMap(tuple -> createCardImageEmbed(tuple, event))
                .then();
    }

    private Mono<Message> createCardImageEmbed(Card card, CardImageEvent event){
        if(card.isEmpty() || card.getUri() == null || card.getName() == null || card.getImage() == null || card.getImage().getNormal() == null){
            return cardUtils.createErrorEmbed(event);
        }

        final Consumer<EmbedCreateSpec> specConsumer = embedCreateSpec -> {
            embedCreateSpec.setColor(Color.GREEN);
            embedCreateSpec.setUrl(card.getUri());
            embedCreateSpec.setTitle(card.getName());
            embedCreateSpec.setImage(card.getImage().getNormal());
        };

        final Response response = Response.with(specConsumer, event.getChannel(), event.getChannelId(),
                event.isFragment(), event.getInlineOrder());
        return messageService.sendOrQueue(event.getChannel(), response);
    }
}
