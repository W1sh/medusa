package com.w1sh.medusa.core;

import com.w1sh.medusa.listeners.EventListener;
import reactor.core.publisher.Mono;

/**
 * A class responsible for find the suitable listener for a given event.
 * See for example {@link CustomEventPublisher} or {@link DiscordEventPublisher}
 *
 * @param <T> The superclass of events to handle
 */
public interface EventPublisher<T> {

    /**
     * Publishes the event of subclass {@link K} to a compatible listener.
     *
     * @param event The event to be published.
     * @param <K> The type of event that is a subclass of {@link T}.
     * @return A {@link Mono} that signals completion upon successful update. If an error is received, it is emitted
     * through the {@code Mono}. If no compatible listener is found, returns an empty {@link Mono}.
     */
    <K extends T> Mono<Void> publish(final K event);

    /**
     * Try and register the {@link EventListener} as a listener of events of type {@link K}.
     * No errors are captured. If event already has a listener it will be overridden.
     *
     * @param listener The {@link EventListener} to register as listener.
     * @param <K> The type of event that is a subclass of {@link T}.
     */
    <K extends T> void registerListener(final EventListener<K, T> listener);
}
