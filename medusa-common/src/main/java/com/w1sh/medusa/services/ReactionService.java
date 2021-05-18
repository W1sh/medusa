package com.w1sh.medusa.services;

import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class ReactionService {

    public Mono<Void> addReactions(Message message, List<ReactionEmoji> reactions) {
        if (reactions.isEmpty()) return Mono.empty();
        Mono<Message> messageMono = Mono.justOrEmpty(message);
        return Flux.fromIterable(reactions)
                .flatMap(reactionEmoji -> messageMono.flatMap(m -> m.addReaction(reactionEmoji)))
                .then();

    }
}
