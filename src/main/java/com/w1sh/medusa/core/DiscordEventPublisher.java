package com.w1sh.medusa.core;

import com.w1sh.medusa.listeners.DiscordEventListener;
import com.w1sh.medusa.listeners.EventListener;
import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public final class DiscordEventPublisher implements EventPublisher<Event> {

    private static final Logger log = LoggerFactory.getLogger(DiscordEventPublisher.class);
    private final Map<Class<? extends Event>, EventListener<? extends Event, Event>> listenerMap = new ConcurrentHashMap<>();
    private final ApplicationContext applicationContext;

    public DiscordEventPublisher(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    private void init() {
        final var listeners = applicationContext.getBeansOfType(DiscordEventListener.class);
        listeners.values().forEach(this::registerListener);
        log.info("Found and registered {} discord event listeners", listeners.size());
    }

    @Override
    public <K extends Event> Mono<Void> publish(K event) {
        Objects.requireNonNull(event);
        log.info("Received new discord event of type <{}>", event.getClass().getSimpleName());

        final var listener = listenerMap.get(event.getClass());
        return Mono.justOrEmpty(event)
                .filter(Objects::nonNull)
                .flatMap(listener::executeIfAssignable);
    }

    @Override
    public <K extends Event> void registerListener(EventListener<K, Event> listener) {
        log.info("Registering discord event listener of type <{}>", listener.getClass().getSimpleName());
        listenerMap.put(listener.getEventType(), listener);
    }
}
