package com.w1sh.medusa.core.listeners.impl;

import com.w1sh.medusa.core.listeners.EventListener;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DisconnectListener implements EventListener<DisconnectEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DisconnectListener.class);

    @Override
    public Class<DisconnectEvent> getEventType() {
        return DisconnectEvent.class;
    }

    @Override
    public Mono<Void> execute(DisconnectEvent event) {
        return Mono.justOrEmpty(event)
                .doOnNext(e -> logger.info("Disconnected from gateway"))
                .then();
    }
}
