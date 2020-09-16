package com.w1sh.medusa.listeners;

import com.w1sh.medusa.validators.LinksRuleValidator;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public final class MessageUpdateEventListener implements DiscordEventListener<MessageUpdateEvent> {

    private final LinksRuleValidator linksRuleValidator;

    @Override
    public Mono<Void> execute(MessageUpdateEvent event) {
        return Mono.just(event)
                .filterWhen(linksRuleValidator::validate)
                .flatMap(ignored -> event.getMessage().flatMap(Message::delete));
    }
}
