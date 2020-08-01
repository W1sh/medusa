package com.w1sh.medusa.core;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.events.Type;
import com.w1sh.medusa.dispatchers.MedusaEventDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public final class Initializer {

    private final MedusaEventDispatcher medusaEventDispatcher;
    private final Reflections reflections;
    private final EventFactory eventFactory;
    private final EventPublisher<Event> eventPublisher;

    private final List<EventListener<?>> listeners;
    private Set<Class<? extends Event>> events;

    @PostConstruct
    public void init(){
        events = reflections.getSubTypesOf(Event.class);
    }

    public void setupDispatcher(final GatewayDiscordClient gateway){
        gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getClass().equals(MessageCreateEvent.class) && event.getMember().map(user -> !user.isBot()).orElse(false))
                .flatMap(event -> Mono.justOrEmpty(eventFactory.extractEvents(event)))
                .flatMap(eventPublisher::publishEvent)
                .subscribe();
    }

    public void registerListeners() {
        listeners.forEach(medusaEventDispatcher::registerListener);
        log.info("Found and registered {} event listeners", listeners.size());
    }

    public void registerEvents() {
        var candidates = events.stream()
                .filter(event -> !Modifier.isAbstract(event.getModifiers()))
                .collect(Collectors.toList());
        for (Class<? extends Event> clazz : candidates) {
            Type type = clazz.getAnnotation(Type.class);
            if(type != null){
                eventFactory.registerEvent(type.prefix(), clazz);
                log.info("Registering new event of type <{}>", clazz.getSimpleName());
            }
            if (!hasListenerRegistered(clazz)) {
                log.warn("Event of type <{}> has no listener registered!", clazz.getSimpleName());
            }
        }
        log.info("Found and registered {} events", events.size());
    }

    private boolean hasListenerRegistered(final Class<? extends Event> clazz){
        return listeners.stream()
                .map(eventListener -> eventListener.getEventType().getSimpleName())
                .anyMatch(eventName -> eventName.equalsIgnoreCase(clazz.getSimpleName()));
    }
}
