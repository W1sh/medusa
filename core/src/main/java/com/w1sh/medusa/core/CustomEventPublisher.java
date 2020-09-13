package com.w1sh.medusa.core;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.EventType;
import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.events.MultipleInlineEvent;
import com.w1sh.medusa.data.events.Type;
import com.w1sh.medusa.services.MessageService;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public final class CustomEventPublisher implements EventPublisher<Event>{

    private final Map<Class<? extends Event>, EventListener<? extends Event, Event>> listenerMap = new ConcurrentHashMap<>();
    private final ApplicationContext applicationContext;
    private final MessageService messageService;
    private final EventService eventService;

    @PostConstruct
    private void init() {
        final var listeners = applicationContext.getBeansOfType(CustomEventListener.class);
        listeners.values().forEach(this::registerListener);
        log.info("Found and registered {} custom event listeners", listeners.size());
    }

    public <T extends Event> Mono<Void> publish(final T event) {
        Objects.requireNonNull(event);
        log.info("Received new custom event of type <{}>", event.getClass().getSimpleName());

        if (event.getClass().equals(MultipleInlineEvent.class)) {
            return publishMulti(((MultipleInlineEvent) event).getEvents());
        } else {
            return publishEvent(event);
        }
    }

    @Override
    public <K extends Event> void registerListener(EventListener<K, Event> listener) {
        log.info("Registering custom event listener of type <{}>", listener.getClass().getSimpleName());
        listenerMap.put(listener.getEventType(), listener);
    }

    public boolean removeListener(final Class<?> clazz) {
        log.info("Removing custom event listener for type <{}>", clazz.getSimpleName());
        final var listener= listenerMap.remove(clazz);
        return listener != null;
    }

    public boolean removeListener(final EventType eventType) {
        log.info("Removing all custom event listeners of type <{}>", eventType.name());
        final var matches = listenerMap.keySet().stream()
                .filter(clazz -> clazz.getAnnotation(Type.class).eventType().equals(eventType))
                .collect(Collectors.toSet());

        matches.forEach(listenerMap::remove);
        return !matches.isEmpty();
    }

    private Mono<Void> publishMulti(final List<InlineEvent> events) {
        messageService.flush((long) events.size());
        return Flux.fromIterable(events)
                .flatMapSequential(this::publish)
                .then();
    }

    private <T extends Event> Mono<Void> publishEvent(final T event) {
        final var listener = listenerMap.get(event.getClass());
        return Mono.justOrEmpty(event)
                .doOnSuccess(eventService::save)
                .filter(Objects::nonNull)
                .flatMap(listener::executeIfAssignable);
    }
}
