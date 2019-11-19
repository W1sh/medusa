package com.w1sh.medusa.core.listeners.impl;

import com.w1sh.medusa.core.listeners.EventListener;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class GenericEventListener implements EventListener<Event, Event> {

    private static final Logger logger = LoggerFactory.getLogger(GenericEventListener.class);

    @Override
    public Class<Event> getEventType() {
        return Event.class;
    }

    @Override
    public Mono<Event> execute(DiscordClient client, Event event) {
        return Mono.just(event).doOnNext(e -> logger.info("Event received: {}", event.getClass().getSimpleName()));
    }
}
