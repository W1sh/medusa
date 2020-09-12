package com.w1sh.medusa.core;

import com.w1sh.medusa.listeners.EventListener;
import reactor.core.publisher.Mono;

public interface EventPublisher<T> {

    <K extends T> Mono<Void> publish(final K event);

    <K extends T> void registerListener(final EventListener<K, T> listener);
}
