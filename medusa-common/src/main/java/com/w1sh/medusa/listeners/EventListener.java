package com.w1sh.medusa.listeners;

import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;

/**
 * A listener responsible to listen to types of events.
 *
 * @param <K> The {@link Class} of event listened by the {@link EventListener}.
 * @param <T> The superclass of event to be listened by the {@link EventListener}.
 */
public interface EventListener<K extends T, T> {

    /**
     * Returns the {@link Class} of event listened by the {@link EventListener}.
     *
     * @return The {@link Class} of event listened by the {@link EventListener}.
     */
    @SuppressWarnings("unchecked")
    default Class<? extends T> getEventType() {
        return (Class<? extends T>) ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }

    /**
     * Executes the logic associated with the event of subclass {@link T} if subclass matches {@link K}, if not
     * returns an empty {@link Mono}.
     * 
     * @param event The event to execute.
     * @return A {@link Mono} that signals completion upon successful update. If an error is received, it is emitted
     * through the {@code Mono}. If the event of type {@link T} is not assignable, returns an empty {@link Mono}.
     */
    @SuppressWarnings("unchecked")
    default Mono<Void> executeIfAssignable(T event) {
        final var clazz = getEventType();
        if (clazz != null && event.getClass().isAssignableFrom(clazz)) {
            return execute((K) event);
        } else return Mono.empty();
    }

    /**
     * Executes the logic associated with the event of type {@link K}.
     *
     * @param event The event of type {@link K} to execute.
     * @return A {@link Mono} that signals completion upon successful update. If an error is received, it is emitted
     * through the {@code Mono}.
     */
    Mono<Void> execute(K event);
}
