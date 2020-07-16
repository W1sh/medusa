package com.w1sh.medusa.rules;

import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.Type;
import com.w1sh.medusa.data.responses.Response;
import com.w1sh.medusa.data.responses.TextMessage;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
public final class NoGamblingRule implements Function<MessageCreateEvent, Mono<? extends Response>> {

    @Override
    public Mono<? extends Response> apply(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getClass().getAnnotation(Type.class))
                .filter(type -> EventType.GAMBLING.equals(type.eventType()))
                .doOnNext(ignored -> event.getMessage().delete().subscribe())
                .flatMap(ignored -> createNoGamblingMessage(event));
    }

    private Mono<TextMessage> createNoGamblingMessage(MessageCreateEvent event){
        return event.getMessage().getChannel()
                .map(chan -> new TextMessage(chan, String.format("**%s**, no gambling allowed on this channel",
                        event.getMember().map(Member::getDisplayName).orElse("")), false));
    }
}
