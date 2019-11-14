package com.w1sh.medusa.listeners;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface EventListener<T extends Event, S> {

    Class<T> getEventType();

    Mono<S> execute(DiscordClient client, T event);
}
