package com.w1sh.medusa.listeners.impl;

import com.w1sh.medusa.listeners.EventListener;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GenericEventListener implements EventListener<Event> {

    @Override
    public Class<Event> getEventType() {
        return Event.class;
    }

    @Override
    public Mono<Void> execute(DiscordClient client, Event event) {
        return Mono.just(event)
                .doOnNext(e -> log.info("Event received: {}", event.getClass().getSimpleName()))
                .then();
    }
}
