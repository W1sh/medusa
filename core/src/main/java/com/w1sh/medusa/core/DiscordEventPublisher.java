package com.w1sh.medusa.core;

import com.w1sh.medusa.listeners.DiscordEventListener;
import discord4j.core.event.domain.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public final class DiscordEventPublisher {

    private final Map<Class<? extends Event>, DiscordEventListener<? extends Event>> listenerMap = new ConcurrentHashMap<>();
    private final ApplicationContext applicationContext;

    @PostConstruct
    private void init() {
        final var listeners = applicationContext.getBeansOfType(DiscordEventListener.class);
        listeners.values().forEach(this::registerListener);
        log.info("Found and registered {} event listeners", listeners.size());
    }

    public <T extends Event> Mono<Void> publishEvent(final T event) {
        Objects.requireNonNull(event);
        log.info("Received new event of type <{}>", event.getClass().getSimpleName());

        final var listener = listenerMap.get(event.getClass());
        return Mono.justOrEmpty(event)
                .filter(e -> listener != null)
                .flatMap(listener::executeIfAssignable);
    }

    public <T extends Event> void registerListener(final DiscordEventListener<T> listener) {
        log.info("Registering event listener of type <{}>", listener.getClass().getSimpleName());
        listenerMap.put(listener.getEventType(), listener);
    }
}
