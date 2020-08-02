package com.w1sh.medusa.core;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.events.Type;
import com.w1sh.medusa.validators.Validator;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public final class Initializer {

    private final Reflections reflections;
    private final EventFactory eventFactory;
    private final EventPublisher eventPublisher;
    private final List<Validator> validators;

    private Set<Class<? extends Event>> events;

    @PostConstruct
    public void init(){
        events = reflections.getSubTypesOf(Event.class);
    }

    public void setupDispatcher(final GatewayDiscordClient gateway){
        gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getClass().equals(MessageCreateEvent.class) && event.getMember().map(user -> !user.isBot()).orElse(false))
                .flatMap(event -> Mono.justOrEmpty(eventFactory.extractEvents(event)))
                .filterWhen(ev -> Flux.fromIterable(validators)
                        .flatMap(validator -> validator.validate(ev))
                        .all(Boolean::booleanValue))
                .flatMap(eventPublisher::publishEvent)
                .subscribe();
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
        }
        log.info("Found and registered {} events", events.size());
    }
}
