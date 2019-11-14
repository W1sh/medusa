package com.w1sh.medusa.listeners.impl;

import com.w1sh.medusa.listeners.EventListener;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DisconnectListener implements EventListener<DisconnectEvent, Void> {

    @Override
    public Class<DisconnectEvent> getEventType() {
        return DisconnectEvent.class;
    }

    @Override
    public Mono<Void> execute(DiscordClient client, DisconnectEvent event) {
        return Mono.justOrEmpty(event)
                .doOnNext(e -> log.info("Disconnected from gateway"))
                .then();
    }
}
