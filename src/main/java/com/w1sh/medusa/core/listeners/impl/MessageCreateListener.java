package com.w1sh.medusa.core.listeners.impl;

import com.w1sh.medusa.core.listeners.EventListener;
import com.w1sh.medusa.managers.CommandManager;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static java.util.function.Predicate.not;

@Component
public class MessageCreateListener implements EventListener<MessageCreateEvent, Void> {

    private static final Logger logger = LoggerFactory.getLogger(MessageCreateListener.class);

    private final CommandManager commandManager;

    public MessageCreateListener(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<Void> execute(MessageCreateEvent event) {
        return Mono.justOrEmpty(event.getMessage())
                .filter(m -> m.getContent().map(msg -> msg.startsWith("!")).orElse(false))
                .flatMap(Message::getAuthorAsMember)
                .filter(not(User::isBot))
                .filterWhen(member -> member.getRoles()
                        .filter(Objects::nonNull)
                        .map(Role::getName)
                        .doOnEach(role -> logger.info("Role {}", role.get()))
                        .any(role -> role.toLowerCase().contains("admin")))
                .flatMap(m -> Mono.just(event))
                .doOnNext(commandManager::process)
                //.map(Tuple2::getT2)
                /*.doOnNext(channel -> channel.createMessage("Welcome")
                        .elapsed()
                        .map(Tuple2::getT1)
                        .doOnNext(elapsed -> log.info("Answered request in {} milliseconds", elapsed))
                        .subscribe())*/
                .then();
    }
}
