package com.w1sh.medusa.core.listeners;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface EventListener<T extends Event> {

    Class<T> getEventType();

    Mono<Void> execute(T event);

    default Mono<Boolean> validate(T event) { return Mono.just(true); }
}
