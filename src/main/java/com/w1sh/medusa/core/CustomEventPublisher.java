package com.w1sh.medusa.core;

import com.w1sh.medusa.data.Event;
import com.w1sh.medusa.data.events.MultipleInlineEvent;
import com.w1sh.medusa.listeners.CustomEventListener;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.services.EventService;
import com.w1sh.medusa.services.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
            return publishMulti((MultipleInlineEvent) event);
        } else {
            return publishEvent(event);
        }
    }

    @Override
    public <K extends Event> void registerListener(EventListener<K, Event> listener) {
        log.info("Registering custom event listener of type <{}>", listener.getClass().getSimpleName());
        listenerMap.put(listener.getEventType(), listener);
    }

    private Mono<Void> publishMulti(final MultipleInlineEvent event) {
        return Flux.fromIterable(event.getEvents())
                .flatMapSequential(this::publish)
                .doOnEach(voidSignal -> {
                    if (voidSignal.getType().equals(SignalType.ON_COMPLETE)) {
                        messageService.flush(event.getChannelId());
                    }
                }).then();
    }

    private <T extends Event> Mono<Void> publishEvent(final T event) {
        final var listener = listenerMap.get(event.getClass());
        return Mono.justOrEmpty(event)
                .doOnSuccess(eventService::save)
                .filter(Objects::nonNull)
                .flatMap(listener::executeIfAssignable);
    }
}