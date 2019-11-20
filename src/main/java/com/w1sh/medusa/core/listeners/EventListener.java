package com.w1sh.medusa.core.listeners;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public interface EventListener<T extends Event> {

    Class<T> getEventType();

    Mono<Void> execute(T event);

}
