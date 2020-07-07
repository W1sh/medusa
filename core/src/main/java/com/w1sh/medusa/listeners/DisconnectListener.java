package com.w1sh.medusa.listeners;

import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public final class DisconnectListener implements EventListener<DisconnectEvent> {

    @Override
    public Class<DisconnectEvent> getEventType() {
        return DisconnectEvent.class;
    }

    @Override
    public Mono<Void> execute(DisconnectEvent event) {
        return Mono.justOrEmpty(event)
                .doOnNext(e -> log.info("Disconnected from gateway"))
                .then();
    }
}
