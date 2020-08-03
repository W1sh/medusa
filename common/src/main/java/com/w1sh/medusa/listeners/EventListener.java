package com.w1sh.medusa.listeners;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;

public interface EventListener<T extends Event> {

    @SuppressWarnings("unchecked")
    default Class<? extends Event> getEventType() {
        return (Class<? extends Event>) ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }

    Mono<Void> execute(T event);

    @SuppressWarnings("unchecked")
    default Mono<Void> executeIfAssignable(Event event) {
        final var clazz = getEventType();
        if (clazz != null && event.getClass().isAssignableFrom(clazz)) {
            return execute((T) event);
        } else return Mono.empty();
    }
}
