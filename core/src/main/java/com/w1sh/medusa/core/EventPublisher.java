package com.w1sh.medusa.core;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.validators.Validator;
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
public final class EventPublisher<T extends Event> {

    private final Map<Class<T>, EventListener<T>> listenerMap = new HashMap<>();
    private final List<EventListener<T>> listeners;
    private final List<Validator> validators;

    @PostConstruct
    private void init(){
        listeners.forEach(this::registerListener);
    }

    public Mono<Void> publishEvent(final T event) {
        Objects.requireNonNull(event);
        log.info("Received new event of type <{}>", event.getClass().getSimpleName());
        final EventListener<T> listener = listenerMap.get(event.getClass());

        return Mono.justOrEmpty(event)
                .ofType(listener.getEventType())
                .filterWhen(ev -> Flux.fromIterable(validators)
                        .flatMap(validator -> validator.validate(ev))
                        .all(Boolean::booleanValue))
                .flatMap(listener::execute);
    }

    public void registerListener(final EventListener<T> listener) {
        log.info("Registering event listener of type <{}>", listener.getClass());
        listenerMap.put(listener.getEventType(), listener);
    }

    public void removeListener(final Event event) {
        log.info("Removing event listener for type <{}>", event.getClass().getSimpleName());
        listenerMap.remove(event.getClass());
    }

}
