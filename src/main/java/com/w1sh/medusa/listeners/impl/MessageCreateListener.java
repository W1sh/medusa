package com.w1sh.medusa.listeners.impl;

import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.managers.CmdController;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Objects;

import static java.util.function.Predicate.*;

@Slf4j
@Component
public class MessageCreateListener implements EventListener<MessageCreateEvent> {

    private final CmdController cmdController;

    public MessageCreateListener(CmdController cmdController) {
        this.cmdController = cmdController;
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
                            .doOnEach(role -> log.info("Role {}", role.get()))
                            .any(role -> role.toLowerCase().contains("admin")))
                .flatMap(m -> Mono.just(event))
                .doOnNext(cmdController::process)
                //.map(Tuple2::getT2)
                /*.doOnNext(channel -> channel.createMessage("Welcome")
                        .elapsed()
                        .map(Tuple2::getT1)
                        .doOnNext(elapsed -> log.info("Answered request in {} milliseconds", elapsed))
                        .subscribe())*/
                .then();
    }
}
