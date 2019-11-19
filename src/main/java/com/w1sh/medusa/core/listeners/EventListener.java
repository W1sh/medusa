package com.w1sh.medusa.core.listeners;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface EventListener<T extends Event, S> {

    Class<T> getEventType();

    Mono<S> execute(T event);
}
