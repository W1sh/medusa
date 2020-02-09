package com.w1sh.medusa.utils;

import com.w1sh.medusa.data.events.Event;
import com.w1sh.medusa.data.events.Registered;
import com.w1sh.medusa.dispatchers.MedusaEventDispatcher;
import com.w1sh.medusa.listeners.EventListener;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class EventDispatcherInitializer {

    private static final Logger logger = LoggerFactory.getLogger(EventDispatcherInitializer.class);

    private final ApplicationContext applicationContext;
    private final MedusaEventDispatcher medusaEventDispatcher;

    public EventDispatcherInitializer(ApplicationContext applicationContext, MedusaEventDispatcher medusaEventDispatcher) {
        this.applicationContext = applicationContext;
        this.medusaEventDispatcher = medusaEventDispatcher;
    }

    public void registenListeners() {
        final var listeners = applicationContext.getBeansOfType(EventListener.class).values();
        listeners.forEach(medusaEventDispatcher::registerListener);
        logger.info("Found and registered {} event listeners", listeners.size());
    }

    public void registerEvents() {
        var reflections = new Reflections("com.w1sh.medusa");
        var classes = reflections.getTypesAnnotatedWith(Registered.class);
        // TODO: register events to event factory
    }
}
