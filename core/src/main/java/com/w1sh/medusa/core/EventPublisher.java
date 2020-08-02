package com.w1sh.medusa.core;

import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.events.MultipleInlineEvent;
import com.w1sh.medusa.data.events.Type;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import discord4j.core.event.domain.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final Map<Class<? extends Event>, EventListener<? extends Event>> listenerMap = new HashMap<>();
    private final ApplicationContext applicationContext;
    private final ResponseDispatcher responseDispatcher;

    @PostConstruct
    private void init() {
        final var listeners = applicationContext.getBeansOfType(EventListener.class);
        listeners.values().forEach(this::registerListener);
        log.info("Found and registered {} event listeners", listeners.size());
    }

    public <T extends Event> Mono<Void> publishEvent(final T event) {
        Objects.requireNonNull(event);
        log.info("Received new event of type <{}>", event.getClass().getSimpleName());

        if (event.getClass().equals(MultipleInlineEvent.class)) {
            return publishMulti(((MultipleInlineEvent) event).getEvents());
        } else {
            return publish(event);
        }
    }

    public <T extends Event> void registerListener(final EventListener<T> listener) {
        log.info("Registering event listener of type <{}>", listener.getClass().getSimpleName());
        listenerMap.put(listener.getEventType(), listener);
    }

    public boolean removeListener(final Class<?> clazz) {
        log.info("Removing event listener for type <{}>", clazz.getSimpleName());
        final var listener= listenerMap.remove(clazz);
        return listener != null;
    }

    public boolean removeListener(final EventType eventType) {
        log.info("Removing all event listeners of type <{}>", eventType.name());
        final var matches = listenerMap.keySet().stream()
                .filter(clazz -> clazz.getAnnotation(Type.class).eventType().equals(eventType))
                .collect(Collectors.toSet());

        matches.forEach(listenerMap::remove);
        return !matches.isEmpty();
    }

    private Mono<Void> publishMulti(final List<InlineEvent> events) {
        responseDispatcher.flush((long) events.size());
        return Flux.fromIterable(events)
                .flatMapSequential(this::publish)
                .then();
    }

    private <T extends Event> Mono<Void> publish(final T event) {
        final var listener = listenerMap.get(event.getClass());

        return Mono.justOrEmpty(event)
                .filter(e -> listener != null)
                .flatMap(listener::executeIfAssignable);
    }
}
