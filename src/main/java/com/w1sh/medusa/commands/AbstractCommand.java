package com.w1sh.medusa.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public abstract class AbstractCommand {

    public abstract String getName();

    public abstract String getDescription();

    public abstract boolean isAdminOnly();

    public abstract Mono<Void> execute(MessageCreateEvent event);

}
