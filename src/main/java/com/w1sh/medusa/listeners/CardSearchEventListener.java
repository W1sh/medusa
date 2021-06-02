package com.w1sh.medusa.listeners;

import com.w1sh.medusa.data.responses.OutputEmbed;
import com.w1sh.medusa.events.CardSearchEvent;
import com.w1sh.medusa.output.ErrorEmbed;
import com.w1sh.medusa.output.SearchEmbed;
import com.w1sh.medusa.rest.resources.Card;
import com.w1sh.medusa.services.CardService;
import com.w1sh.medusa.services.MessageService;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public final class CardSearchEventListener implements CustomUpdatableEventListener<CardSearchEvent> {

    private final CardService cardService;
    private final MessageService messageService;

    @Override
    public Mono<Void> execute(CardSearchEvent event) {
        if (event.isInvalid()) return messageService.sendOrQueue(event.getChannel(), new ErrorEmbed(event));
        return cardService.getCardsByName(event.getInlineArgument())
                .doOnNext(this::filterList)
                .map(list -> new SearchEmbed(list, event))
                .flatMap(embed -> messageService.sendOrQueue(event.getChannel(), embed))
                .onErrorResume(t -> messageService.sendOrQueue(event.getChannel(), new ErrorEmbed(event)));
    }

    @Override
    public Mono<Void> update(ReactionAddEvent event) {
        return messageService.getCached(event.getMessageId().asString())
                .filter(tuple -> tuple.getT2().getReactions().contains(event.getEmoji()))
                .flatMap(tuple -> createPage(event, tuple.getT2()))
                .onErrorResume(t -> Mono.empty())
                .then(event.getMessage()
                        .flatMap(message -> message.removeReaction(event.getEmoji(), event.getUserId())));
    }

    private Mono<Message> createPage(ReactionAddEvent event, OutputEmbed outputEmbed) {
        if(event.getEmoji().equals(ReactionEmoji.unicode("\u2B05"))) {
            final SearchEmbed embed = SearchEmbed.previousOf(((SearchEmbed) outputEmbed));
            return messageService.update(event.getMessageId().asString(), embed);
        } else {
            final SearchEmbed embed = SearchEmbed.nextOf(((SearchEmbed) outputEmbed));
            return messageService.update(event.getMessageId().asString(), embed);
        }
    }

    private void filterList(List<Card> cards) {
        cards.removeIf(card -> card.getName() == null || card.getTypeLine() == null || card.getOracleText() == null);
    }
}
