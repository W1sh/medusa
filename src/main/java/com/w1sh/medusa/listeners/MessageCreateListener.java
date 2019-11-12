package com.w1sh.medusa.listeners;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Objects;

@Slf4j
@Component
public class MessageCreateListener implements EventListener<MessageCreateEvent> {

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return event.getMessage()
                .getChannel()
                .zipWith(event.getMessage().getAuthorAsMember())
                .filterWhen(tuple -> tuple.getT2().getRoles()
                            .filter(Objects::nonNull)
                            .map(Role::getName)
                            .doOnEach(role -> log.info("Role {}", role.get()))
                            .any(role -> role.toLowerCase().contains("admin")))
                .map(Tuple2::getT1)
                .doOnNext(textChannel -> textChannel.createMessage("Welcome")
                        .elapsed()
                        .map(Tuple2::getT1)
                        .doOnNext(elapsed -> log.info("Answered request in {} milliseconds", elapsed))
                        .subscribe())
                .then();
    }
}
