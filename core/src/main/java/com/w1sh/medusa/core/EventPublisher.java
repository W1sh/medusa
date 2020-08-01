package com.w1sh.medusa.core;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.InlineEvent;
import com.w1sh.medusa.data.events.MultipleInlineEvent;
import com.w1sh.medusa.dispatchers.ResponseDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher<T extends Event> {

    private final Map<Class<T>, EventListener<T>> listenerMap = new HashMap<>();
    private final List<EventListener<T>> listeners;
    private final ResponseDispatcher responseDispatcher;

    @PostConstruct
    private void init(){
        listeners.forEach(this::registerListener);
        log.info("Found and registered {} event listeners", listeners.size());
    }

    public Mono<Void> publishEvent(final T event) {
        Objects.requireNonNull(event);
        log.info("Received new event of type <{}>", event.getClass().getSimpleName());

        if (event.getClass().equals(MultipleInlineEvent.class)) {
            MultipleInlineEvent multipleInlineEvent = (MultipleInlineEvent) event;
            return publishMulti(multipleInlineEvent.getEvents());
        } else {
            return publish(event);
        }
    }

    public void registerListener(final EventListener<T> listener) {
        log.info("Registering event listener of type <{}>", listener.getClass().getSimpleName());
        listenerMap.put(listener.getEventType(), listener);
    }

    public void removeListener(final Event event) {
        log.info("Removing event listener for type <{}>", event.getClass().getSimpleName());
        listenerMap.remove(event.getClass());
    }

    private Mono<Void> publishMulti(final List<InlineEvent> events) {
        responseDispatcher.flush((long) events.size());
        return Flux.fromIterable(events)
                .flatMapSequential(this::publish)
                .then();
    }

    private Mono<Void> publish(final T event) {
        final EventListener<T> listener = listenerMap.get(event.getClass());

        return Mono.justOrEmpty(event)
                .ofType(listener.getEventType())
                .flatMap(listener::execute);
    }

    private Mono<Void> publish(final InlineEvent event) {
        final EventListener<T> listener = listenerMap.get(event.getClass());

        return Mono.justOrEmpty(event)
                .ofType(listener.getEventType())
                .flatMap(listener::execute);
    }
}
