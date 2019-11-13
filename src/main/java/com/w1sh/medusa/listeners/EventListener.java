package com.w1sh.medusa.listeners;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface EventListener<T extends Event> {

    Class<T> getEventType();

    Mono<Void> execute(DiscordClient client, T event);
}
