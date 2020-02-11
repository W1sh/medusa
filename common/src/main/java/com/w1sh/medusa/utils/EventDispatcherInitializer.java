package com.w1sh.medusa.utils;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.EventFactory;
import com.w1sh.medusa.data.events.Registered;
import com.w1sh.medusa.dispatchers.MedusaEventDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import com.w1sh.medusa.validators.ArgumentValidator;
import com.w1sh.medusa.validators.PermissionsValidator;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.bool.BooleanUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

@Component
public class EventDispatcherInitializer {

    private static final Logger logger = LoggerFactory.getLogger(EventDispatcherInitializer.class);

    private final ApplicationContext applicationContext;
    private final MedusaEventDispatcher medusaEventDispatcher;
    private final Reflections reflections;
    private final ArgumentValidator argumentValidator;
    private final PermissionsValidator permissionsValidator;


    private Set<EventListener> listeners;
    private Set<Class<? extends Event>> events;

    public EventDispatcherInitializer(ApplicationContext applicationContext, MedusaEventDispatcher medusaEventDispatcher,
                                      Reflections reflections, ArgumentValidator argumentValidator, PermissionsValidator permissionsValidator) {
        this.applicationContext = applicationContext;
        this.medusaEventDispatcher = medusaEventDispatcher;
        this.reflections = reflections;
        this.argumentValidator = argumentValidator;
        this.permissionsValidator = permissionsValidator;
    }

    @PostConstruct
    public void init(){
        listeners = findAllListeners();
        events = findAllEvents();
    }

    public void setupDispatcher(GatewayDiscordClient gateway){
        gateway.on(MessageCreateEvent.class)
                .filter(event -> event.getClass().equals(MessageCreateEvent.class) &&
                        event.getMember().isPresent() && event.getMember().map(user -> !user.isBot()).orElse(false))
                .map(EventFactory::extractEvents)
                .filterWhen(ev -> BooleanUtils.and(argumentValidator.validate(ev), permissionsValidator.validate(ev)))
                .doOnSubscribe(ev -> logger.info("Received new event of type <{}>", ev.getClass().getSimpleName()))
                .subscribe(medusaEventDispatcher::publish);
    }

    public void registerListeners() {
        listeners.forEach(medusaEventDispatcher::registerListener);
        logger.info("Found and registered {} event listeners", listeners.size());
    }

    public void registerEvents() {
        for (Class<? extends Event> clazz : events) {
            Registered registered = clazz.getAnnotation(Registered.class);
            if(registered != null){
                logger.info("Registering new event of type <{}>", clazz.getSimpleName());
                String prefix = registered.prefix();
                EventFactory.registerEvent(prefix, clazz);
            }
            boolean hasListener = listeners.stream()
                    .map(eventListener -> eventListener.getEventType().getSimpleName())
                    .anyMatch(eventName -> eventName.equalsIgnoreCase(clazz.getSimpleName()));
            if (!hasListener && !Modifier.isAbstract(clazz.getModifiers())) {
                logger.warn("Event of type <{}> has no listener registered!", clazz.getSimpleName());
            }
        }
        logger.info("Found and registered {} event listeners", events.size());
    }

    private Set<EventListener> findAllListeners(){
        return new HashSet<>(applicationContext.getBeansOfType(EventListener.class).values());
    }

    private Set<Class<? extends Event>> findAllEvents(){
        return reflections.getSubTypesOf(Event.class);
    }
}
